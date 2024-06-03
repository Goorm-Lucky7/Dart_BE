package com.dart.api.application.member;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dart.api.domain.auth.AuthUser;
import com.dart.api.domain.member.entity.Member;
import com.dart.api.domain.member.repo.MemberRepository;
import com.dart.dto.member.request.MemberUpdateDto;
import com.dart.dto.member.request.SignUpDto;
import com.dart.dto.member.response.MemberProfileResDto;
import com.dart.global.error.exception.ConflictException;
import com.dart.global.error.exception.NotFoundException;
import com.dart.global.error.model.ErrorCode;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

	private final MemberRepository memberRepository;
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
	public void updateMemberProfile(AuthUser authUser, MemberUpdateDto memberUpdateDto) {
		final Member member = findMemberByEmail(authUser.email());
		final String encodedPassword = passwordEncoder.encode(memberUpdateDto.password());

		validateNicknameForUpdate(memberUpdateDto.nickname());
		member.updateMemberProfile(memberUpdateDto, encodedPassword);
	}

	private Member findMemberByEmail(String email) {
		return memberRepository.findByEmail(email)
			.orElseThrow(() -> new NotFoundException(ErrorCode.FAIL_MEMBER_NOT_FOUND));
	}

	private MemberProfileResDto convertToMemberProfileResDto(Member member) {
		return new MemberProfileResDto(member.getEmail(), member.getNickname(), member.getProfileImage(),
			member.getBank(), member.getAccount(), member.getIntroduce());
	}

	private void validateNickname(String nickname) {
		if(memberRepository.existsByNickname(nickname)){
			throw new ConflictException(ErrorCode.FAIL_NICKNAME_CONFLICT);
		}
	}

	private void validateNicknameForUpdate(String nickname) {
		if(nickname != null && !nickname.trim().isEmpty()) {
			validateNickname(nickname);
		}
	}

}