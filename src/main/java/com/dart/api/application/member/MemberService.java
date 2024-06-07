package com.dart.api.application.member;

import java.io.IOException;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

import com.dart.api.domain.auth.entity.AuthUser;
import com.dart.api.domain.member.entity.Member;
import com.dart.api.domain.member.repository.MemberRepository;
import com.dart.api.dto.member.request.MemberUpdateDto;
import com.dart.api.dto.member.request.NicknameDuplicationCheckDto;
import com.dart.api.dto.member.request.SignUpDto;
import com.dart.api.dto.member.response.MemberProfileResDto;
import com.dart.global.common.util.S3Service;
import com.dart.global.error.exception.BadRequestException;
import com.dart.global.error.exception.NotFoundException;
import com.dart.global.error.model.ErrorCode;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

	private final MemberRepository memberRepository;
	private final S3Service s3Service;
	private final NicknameValidator nicknameValidator;
	private final PasswordEncoder passwordEncoder;

	@Transactional
	public void signUp(SignUpDto signUpDto) {
		final String encodedPassword = passwordEncoder.encode(signUpDto.password());
		final Member member = Member.signup(signUpDto, encodedPassword);

		memberRepository.save(member);
	}

	public MemberProfileResDto getMemberProfile(AuthUser authUser) {
		final Member member = findMemberByEmail(authUser.email());

		return convertToMemberProfileResDto(member);
	}

	@Transactional
	public void updateMemberProfile(AuthUser authUser, MemberUpdateDto memberUpdateDto, MultipartFile profileImage) {
		final Member member = findMemberByEmail(authUser.email());
		nicknameValidator.validate(memberUpdateDto.nickname());

		try{
			String profileImageUrl = s3Service.uploadFile(profileImage);
			member.updateMemberProfile(memberUpdateDto, profileImageUrl);
		} catch (IOException e) {
			throw new BadRequestException(ErrorCode.FAIL_INVALID_REQUEST);
		}
	}

	public void checkNicknameDuplication(NicknameDuplicationCheckDto nicknameDuplicationCheckDto) {
		nicknameValidator.validate(nicknameDuplicationCheckDto.nickname());
	}

	private Member findMemberByEmail(String email) {
		return memberRepository.findByEmail(email)
			.orElseThrow(() -> new NotFoundException(ErrorCode.FAIL_MEMBER_NOT_FOUND));
	}

	private MemberProfileResDto convertToMemberProfileResDto(Member member) {
		return new MemberProfileResDto(member.getEmail(), member.getNickname(), member.getIntroduce(), member.getProfileImageUrl());
	}
}
