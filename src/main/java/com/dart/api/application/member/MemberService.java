package com.dart.api.application.member;

import static java.lang.Boolean.*;

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
import com.dart.api.domain.auth.repository.EmailRedisRepository;
import com.dart.api.domain.auth.repository.NicknameRedisRepository;
import com.dart.api.domain.auth.repository.SessionRedisRepository;
import com.dart.api.dto.member.response.MemberSimpleProfileResDto;
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
	private final EmailRedisRepository emailRedisRepository;
	private final NicknameRedisRepository nicknameRedisRepository;
	private final SessionRedisRepository sessionRedisRepository;

	private final S3Service s3Service;
	private final NicknameService nicknameService;
	private final PasswordEncoder passwordEncoder;

	@Transactional
	public void signUp(SignUpDto signUpDto, String sessionId) {
		nicknameService.setNicknameVerified(signUpDto.nickname());

		verifyEmailChecked(signUpDto.email());
		verifyNicknameChecked(signUpDto.nickname());
		validateMemberNotDuplicated(signUpDto.email());

		final String encodedPassword = passwordEncoder.encode(signUpDto.password());
		final Member member = Member.signup(signUpDto, encodedPassword);

		memberRepository.save(member);

		cleanUpSessionData(sessionId, signUpDto.email(), signUpDto.nickname());
	}

	public MemberProfileResDto getMemberProfile(String nickname, AuthUser authUser) {
		validateMemberExists(nickname);
		if(isOwnProfile(authUser.nickname(), nickname)) {
			return getOwnProfile(nickname);
		} else {
			return getOtherProfile(nickname);
		}
	}

	@Transactional
	public MemberSimpleProfileResDto updateMemberProfile(
		AuthUser authUser,
		MemberUpdateDto memberUpdateDto,
		MultipartFile profileImage,
		String sessionId) {

		Member member = findMemberByEmail(authUser.email());
		validateNickname(memberUpdateDto.nickname(), member.getNickname(), sessionId);
		String newProfileImageUrl = handleProfileImageUpdate(profileImage, member.getProfileImageUrl());

		member.updateProfile(memberUpdateDto, newProfileImageUrl);
		handleNicknameUpdate(memberUpdateDto.nickname(), member.getNickname(), sessionId);

		return new MemberSimpleProfileResDto(member.getEmail(), member.getNickname(), member.getProfileImageUrl());
	}

	public void checkNicknameDuplication(NicknameDuplicationCheckDto nicknameDuplicationCheckDto, String sessionId,
		HttpServletResponse response) {
		nicknameService.checkAndReserveNickname(nicknameDuplicationCheckDto.nickname(), sessionId, response);
	}

	private void validateNickname(String newNickname, String currentNickname, String sessionId) {
		if (newNickname != null && !newNickname.equals(currentNickname)) {
			if (sessionId == null || !nicknameRedisRepository.isReserved(newNickname)) {
				throw new BadRequestException(ErrorCode.FAIL_NOT_VERIFIED_NICKNAME);
			}
		}
	}

	private String handleProfileImageUpdate(MultipartFile profileImage, String savedProfileImage) {
		if (profileImage == null || profileImage.isEmpty()) return savedProfileImage;

		String newProfileImageUrl = s3Service.uploadFile(profileImage);
		if (savedProfileImage != null) {
			s3Service.deleteFile(savedProfileImage);
		}

		return newProfileImageUrl;
	}

	private void handleNicknameUpdate(String newNickname, String currentNickname, String sessionId) {
		if (newNickname != null && !newNickname.equals(currentNickname)) {
			sessionRedisRepository.deleteSessionNicknameMapping(sessionId);
			nicknameRedisRepository.deleteNickname(newNickname);
		}
	}

	private void cleanUpSessionData(String sessionId, String email, String nickname) {
		sessionRedisRepository.deleteSessionEmailMapping(sessionId);
		sessionRedisRepository.deleteSessionNicknameMapping(sessionId);
		emailRedisRepository.deleteEmail(email);
		nicknameRedisRepository.deleteNickname(nickname);
	}

	private boolean isOwnProfile(String authUserNickname, String profileNickname) {
		return authUserNickname.equals(profileNickname);
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
		if(FALSE.equals(emailRedisRepository.isVerified(email))) {
			throw new BadRequestException(ErrorCode.FAIL_NOT_VERIFIED_EMAIL);
		}
	}

	private void verifyNicknameChecked(String nickname) {
		if(FALSE.equals(nicknameRedisRepository.isReserved(nickname))) {
			throw new BadRequestException(ErrorCode.FAIL_NOT_VERIFIED_NICKNAME);
		}
	}

	private void validateMemberNotDuplicated(String email) {
		if (memberRepository.existsByEmail(email)) {
			throw new ConflictException(ErrorCode.FAIL_EMAIL_CONFLICT);
		}
	}

	private void validateMemberExists(String nickname) {
		if (!memberRepository.existsByNickname(nickname)) {
			throw new NotFoundException(ErrorCode.FAIL_MEMBER_NOT_FOUND);
		}
	}
}
