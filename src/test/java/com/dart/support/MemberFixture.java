package com.dart.support;

import java.time.LocalDate;

import com.dart.api.domain.auth.AuthUser;
import com.dart.api.domain.member.entity.Member;
import com.dart.api.dto.member.request.SignUpDto;

public class MemberFixture {

	public static Member createMemberEntity() {
		return Member.signup(
			createMemberEntityForSignUpDto(),
			"1q2w3e4r!"
		);
	}

	public static SignUpDto createMemberEntityForSignUpDto() {
		return SignUpDto.builder()
			.email("test1@example.com")
			.nickname("test1")
			.password("1q2w3e4r!")
			.birthday(LocalDate.of(2024, 6, 4))
			.bank("example bank")
			.account("000-000-000000")
			.introduce("Hello 👏")
			.build();
	}

	public static AuthUser createAuthUserEntity() {
		return AuthUser.create(
			"test1@example.com",
			"test1"
		);
	}
}
