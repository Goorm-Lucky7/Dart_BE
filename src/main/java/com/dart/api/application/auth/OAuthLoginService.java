package com.dart.api.application.auth;

import static com.dart.global.common.util.OAuthConstant.*;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dart.api.domain.member.entity.Member;
import com.dart.api.domain.member.repository.MemberRepository;
import com.dart.api.dto.member.request.SignUpDto;
import com.dart.global.error.exception.BadRequestException;
import com.dart.global.error.exception.NotFoundException;
import com.dart.global.error.model.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OAuthLoginService {

	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;

	@Transactional
	public Map<String, Object> socialLogin(OAuth2User oauth2User, String oauthProvider) {
		Map<String, String> extractedAttributes = extractAttributesByProvider(oauth2User, oauthProvider);

		String email = extractedAttributes.get("email");
		boolean isNewUser = false;

		if (!isMemberSignedUp(email)) {
			String nickname = extractedAttributes.get("nickname");
			String profileImage = extractedAttributes.get("profileImage");

			signUp(email, nickname, profileImage);
			isNewUser = true;
		}

		Map<String, Object> responseMap = new HashMap<>();
		responseMap.put("isNewUser", isNewUser);
		responseMap.put("member", findMember(email));

		return responseMap;
	}

	private boolean isMemberSignedUp(String email) {
		return memberRepository.existsByEmail(email);
	}

	private Map<String, String> extractAttributesByProvider(OAuth2User oauth2User, String oauthProvider) {
		return switch (oauthProvider) {
			case KAKAO -> extractKakaoAttributes(oauth2User);
			case GOOGLE -> extractGoogleAttributes(oauth2User);
			default -> throw new NotFoundException(ErrorCode.FAIL_REGISTRATION_NOT_FOUND);
		};
	}

	private Map<String, String> extractKakaoAttributes(OAuth2User oauth2User) {
		Map<String, Object> rawAttributes = oauth2User.getAttributes();
		Map<String, String> extractedAttributes = new HashMap<>();
		Map<String, Object> kakaoAccount, profile;

		Object kakaoAccountObj = rawAttributes.get("kakao_account");
		if (kakaoAccountObj instanceof Map) {
			kakaoAccount = (Map<String, Object>) kakaoAccountObj;
			Object profileObj = kakaoAccount.get("profile");
			if (profileObj instanceof Map) {
				profile = (Map<String, Object>) profileObj;
			} else {
				throw new BadRequestException(ErrorCode.FAIL_INVALID_REQUEST);
			}
		} else {
			throw new BadRequestException(ErrorCode.FAIL_INVALID_REQUEST);
		}

		String rawPassword = generateRandomPassword();
		String encodedPassword = passwordEncoder.encode(rawPassword);

		extractedAttributes.put("email", String.valueOf(kakaoAccount.get("email")));
		extractedAttributes.put("nickname", String.valueOf(profile.get("nickname")));
		extractedAttributes.put("profileImage", String.valueOf(profile.get("profile_image_url")));
		extractedAttributes.put("password", encodedPassword);

		return extractedAttributes;
	}

	private Map<String, String> extractGoogleAttributes(OAuth2User oauth2User) {
		Map<String, Object> rawAttributes = oauth2User.getAttributes();
		Map<String, String> extractedAttributes = new HashMap<>();

		String email = (String)rawAttributes.get("email");
		String nickname = (String)rawAttributes.get("given_name");
		String profileImage = (String)rawAttributes.get("picture");
		String rawPassword = generateRandomPassword();
		String encodedPassword = passwordEncoder.encode(rawPassword);

		extractedAttributes.put("email", email);
		extractedAttributes.put("nickname", nickname);
		extractedAttributes.put("profileImage", profileImage);
		extractedAttributes.put("password", encodedPassword);

		return extractedAttributes;
	}

	private void signUp(String email, String nickname, String profileImage) {
		String password = passwordEncoder.encode(generateRandomPassword());
		Member member = Member.signup(new SignUpDto(email, nickname, password, null, null), password);
		memberRepository.save(member);
		member.updateProfileImage(profileImage);
	}

	private String generateRandomPassword() {
		SecureRandom random = new SecureRandom();
		byte[] bytes = new byte[16];
		random.nextBytes(bytes);
		return Base64.getEncoder().encodeToString(bytes);
	}

	private Member findMember(String email) {
		return memberRepository.findByEmail(email).orElse(null);
	}
}