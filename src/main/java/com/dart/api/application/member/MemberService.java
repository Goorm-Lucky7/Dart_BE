package com.dart.api.application.member;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;;
import com.dart.api.domain.member.entity.ProfileImage;
import com.dart.api.domain.member.entity.Member;
import com.dart.api.domain.member.repo.MemberRepository;
import com.dart.api.domain.member.repo.ProfileImageRepository;
import com.dart.dto.member.request.SignUpDto;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final ProfileImageRepository profileImageRepository;

	@Transactional
	public void signUp(SignUpDto signUpDto) {
		final String encodedPassword = passwordEncoder.encode(signUpDto.password());
		final Member member = Member.signup(signUpDto, encodedPassword);
		final ProfileImage profileImage = ProfileImage.createDefaultForMember(member);

		memberRepository.save(member);
		profileImageRepository.save(profileImage);
	}

}