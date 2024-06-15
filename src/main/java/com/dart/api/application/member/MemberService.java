package com.dart.api.application.member;

import static java.lang.Boolean.*;

import java.io.IOException;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import com.dart.api.domain.auth.entity.AuthUser;
import com.dart.api.domain.member.entity.Member;
import com.dart.api.domain.member.repository.MemberRepository;
import com.dart.api.dto.member.request.MemberUpdateDto;
import com.dart.api.dto.member.request.NicknameDuplicationCheckDto;
import com.dart.api.dto.member.request.SignUpDto;
import com.dart.api.dto.member.response.MemberProfileResDto;
import com.dart.api.infrastructure.redis.RedisEmailRepository;
import com.dart.api.infrastructure.redis.RedisNicknameRepository;
import com.dart.api.infrastructure.redis.RedisSessionRepository;
import com.dart.api.infrastructure.s3.S3Service;
import com.dart.global.error.exception.BadRequestException;
import com.dart.global.error.exception.ConflictException;
import com.dart.global.error.exception.NotFoundException;
import com.dart.global.error.model.ErrorCode;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

	private final MemberRepository memberRepository;
	private final RedisEmailRepository redisEmailRepository;
	private final RedisNicknameRepository redisNicknameRepository;
	private final RedisSessionRepository redisSessionRepository;

	private final S3Service s3Service;
	private final NicknameService nicknameService;
	private final PasswordEncoder passwordEncoder;

	@Transactional
	public void signUp(SignUpDto signUpDto, String sessionId) {
		nicknameService.setNicknameVerified(signUpDto.nickname());

		verifyEmailChecked(signUpDto.email());
		verifyNicknameChecked(signUpDto.nickname());
		validateExistMember(signUpDto.email());

		final String encodedPassword = passwordEncoder.encode(signUpDto.password());
		final Member member = Member.signup(signUpDto, encodedPassword);

		memberRepository.save(member);

		cleanUpSessionData(sessionId, signUpDto.email(), signUpDto.nickname());
	}

	public MemberProfileResDto getMemberProfile(String nickname, AuthUser authUser) {
		if(isMember(authUser) && isOwnProfile(nickname, authUser.nickname())) {
			return getOwnProfile(nickname);
		} else {
			return getOtherProfile(nickname);
		}
	}

	@Transactional
	public void updateMemberProfile(AuthUser authUser, MemberUpdateDto memberUpdateDto, MultipartFile profileImage,
		String sessionId, HttpServletResponse response) {
		String newNickname = memberUpdateDto.nickname();
		final Member member = findMemberByEmail(authUser.email());

		if (newNickname != null && !newNickname.equals(member.getNickname())) {
			if (sessionId == null || !redisNicknameRepository.isReserved(newNickname)) {
				throw new BadRequestException(ErrorCode.FAIL_NOT_VERIFIED_NICKNAME);
			}
		}

		final String savedProfileImage = member.getProfileImageUrl();
		String newProfileImageUrl = null;

		try {
			if (profileImage != null && !profileImage.isEmpty()) {
				newProfileImageUrl = s3Service.uploadFile(profileImage);
				if (savedProfileImage != null) {
					s3Service.deleteFile(savedProfileImage);
				}
			}

			member.updateMemberProfile(memberUpdateDto, newProfileImageUrl);

			if (newNickname != null && !newNickname.equals(member.getNickname())) {
				redisSessionRepository.deleteSessionNicknameMapping(sessionId);
				redisNicknameRepository.deleteNickname(newNickname);
			}

		} catch (IOException e) {
			throw new BadRequestException(ErrorCode.FAIL_INVALID_REQUEST);
		}
	}

	public void checkNicknameDuplication(NicknameDuplicationCheckDto nicknameDuplicationCheckDto, String sessionId,
		HttpServletResponse response) {
		nicknameService.checkAndReserveNickname(nicknameDuplicationCheckDto.nickname(), sessionId, response);
	}

	private boolean isMember(AuthUser authUser) {
		return authUser != null;
	}

	private void cleanUpSessionData(String sessionId, String email, String nickname) {
		redisSessionRepository.deleteSessionEmailMapping(sessionId);
		redisSessionRepository.deleteSessionNicknameMapping(sessionId);
		redisEmailRepository.deleteEmail(email);
		redisNicknameRepository.deleteNickname(nickname);
	}

	private boolean isOwnProfile(String currentNickname, String profileNickname) {
		return currentNickname.equals(profileNickname);
	}

	private MemberProfileResDto getOwnProfile(String nickname) {
		final Member member = findMemberByNickname(nickname);
		return new MemberProfileResDto(member.getEmail(), member.getNickname(), member.getProfileImageUrl(),
			String.valueOf(member.getBirthday()), member.getIntroduce());
	}

	private MemberProfileResDto getOtherProfile(String nickname) {
		final Member member = findMemberByNickname(nickname);
		return new MemberProfileResDto(member.getEmail(), member.getNickname(), member.getProfileImageUrl(),
			null, member.getIntroduce());
	}

	private Member findMemberByNickname(String nickname) {
		return memberRepository.findByNickname(nickname)
			.orElseThrow(() -> new NotFoundException(ErrorCode.FAIL_MEMBER_NOT_FOUND));
	}

	private Member findMemberByEmail(String email) {
		return memberRepository.findByEmail(email)
			.orElseThrow(() -> new NotFoundException(ErrorCode.FAIL_MEMBER_NOT_FOUND));
	}

	private void verifyEmailChecked(String email) {
		if(FALSE.equals(redisEmailRepository.isVerified(email))) {
			throw new BadRequestException(ErrorCode.FAIL_NOT_VERIFIED_EMAIL);
		}
	}

	private void verifyNicknameChecked(String nickname) {
		if(FALSE.equals(redisNicknameRepository.isReserved(nickname))) {
			throw new BadRequestException(ErrorCode.FAIL_NOT_VERIFIED_NICKNAME);
		}
	}

	private void validateExistMember(String email) {
		if (memberRepository.existsByEmail(email)) {
			throw new ConflictException(ErrorCode.FAIL_EMAIL_CONFLICT);
		}
	}
}
