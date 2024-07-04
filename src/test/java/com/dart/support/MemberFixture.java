package com.dart.support;

import java.time.LocalDate;

import com.dart.api.domain.member.entity.Member;
import com.dart.api.dto.member.request.SignUpDto;

public class MemberFixture {

	public static Member createMemberEntity() {
		return Member.signup(
			createSignUpDto("test1@example.com", "test1"),
			"1q2w3e4r!"
		);
	}

	public static Member createMemberEntityWithEmailAndNickname(String email, String nickname) {
		return Member.signup(
			createSignUpDto(email, nickname),
			"1q2w3e4r!"
		);
	}

	public static SignUpDto createSignUpDto(String email, String nickname) {
		return SignUpDto.builder()
			.email(email)
			.nickname(nickname)
			.password("1q2w3e4r!")
			.birthday(LocalDate.of(2024, 6, 4))
			.introduce("Hello 👏")
			.build();
	}
}
