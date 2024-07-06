package com.dart.api.application.auth;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dart.api.domain.member.entity.Member;
import com.dart.api.domain.member.entity.OAuthProvider;
import com.dart.api.domain.member.repository.MemberRepository;
import com.dart.api.dto.auth.request.OAuthProviderUpdateDto;
import com.dart.api.dto.member.request.SignUpDto;
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
		System.out.println("Loading OAuth2 user11");


		String email = extractedAttributes.get("email");
		String nickname = extractedAttributes.get("nickname");
		boolean isNewUser = false;

		if (isMemberSignedUp(email)) {
			if(!isMemberSignedUpWithProvider(email, oauthProvider)){
				updateOAuthProvider(email, oauthProvider);
			}
		} else {
			signUp(email, nickname, oauthProvider);
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

	public boolean isMemberSignedUpWithProvider(String email, String providerName) {
		return memberRepository.existsByEmailAndOauthProvider(email, OAuthProvider.findByName(providerName));
	}
	private Map<String, String> extractAttributesByProvider(OAuth2User oauth2User, String oauthProvider) {
		if ("kakao".equals(oauthProvider)) {
			return extractKakaoAttributes(oauth2User);
		} else {
			throw new NotFoundException(ErrorCode.FAIL_REGISTRATION_NOT_FOUND);
		}
	}

	private Map<String, String> extractKakaoAttributes(OAuth2User oauth2User) {
		Map<String, Object> rawAttributes = oauth2User.getAttributes();
		Map<String, String> extractedAttributes = new HashMap<>();

		Map<String, Object> kakaoAccount, profile;
		kakaoAccount = (Map<String, Object>)rawAttributes.get("kakao_account");
		extractedAttributes.put("email", String.valueOf(kakaoAccount.get("email")));

		profile = (Map<String, Object>) kakaoAccount.get("profile");
		extractedAttributes.put("nickname", String.valueOf(profile.get("nickname")));

		String rawPassword = generateRandomPassword();
		String encodedPassword = passwordEncoder.encode(rawPassword);

		extractedAttributes.put("password", encodedPassword);
		System.out.println("여기까지는 왔니? service");

		return extractedAttributes;
	}

	private Member signUp(String email, String nickname, String oauthProvider) {
		String password = passwordEncoder.encode(generateRandomPassword());
		Member member = Member.signup(new SignUpDto(email, nickname, password, null, null), password);
		memberRepository.save(member);

		member.updateOAuthProvider(new OAuthProviderUpdateDto(oauthProvider));

		return member;
	}

	private void updateOAuthProvider(String email, String providerName) {
		Optional<Member> member = memberRepository.findByEmail(email);
		if (member.isPresent()) {
			Member existingMember = member.get();
			existingMember.updateOAuthProvider(new OAuthProviderUpdateDto(providerName));
		}
	}

	private String generateRandomPassword() {
		SecureRandom random = new SecureRandom();
		byte[] bytes = new byte[16];
		random.nextBytes(bytes);
		return Base64.getEncoder().encodeToString(bytes);
	}

	private Member findMember(String email) {
		Optional<Member> optionalMember = memberRepository.findByEmail(email);
		if (optionalMember.isPresent()) {
			Member member = optionalMember.get();

			return member;
		}
		return null;
	}
}