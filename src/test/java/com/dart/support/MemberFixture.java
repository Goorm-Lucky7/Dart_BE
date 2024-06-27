package com.dart.support;

import java.time.LocalDate;

import com.dart.api.domain.auth.entity.AuthUser;
import com.dart.api.domain.member.entity.Member;
import com.dart.api.dto.member.request.SignUpDto;

public class MemberFixture {

	public static Member createMemberEntity() {
		return Member.signup(
			createMemberEntityForSignUpDto(),
			"1q2w3e4r!"
		);
	}

	public static Member createMemberEntityForAuthor() {
		return Member.signup(
			createMemberEntityForAuthorSignUpDto(),
			"1q2w3e4r!"
		);
	}

	public static SignUpDto createMemberEntityForSignUpDto() {
		return SignUpDto.builder()
			.email("test1@example.com")
			.nickname("test1")
			.password("1q2w3e4r!")
			.birthday(LocalDate.of(2024, 6, 4))
			.introduce("Hello 👏")
			.build();
	}

	public static SignUpDto createMemberEntityForAuthorSignUpDto() {
		return SignUpDto.builder()
			.email("author@example.com")
			.nickname("author")
			.password("1q2w3e4r!")
			.birthday(LocalDate.of(2024, 6, 4))
			.introduce("Have a good time 👏")
			.build();
	}

	public static AuthUser createAuthUserEntity() {
		return AuthUser.create(
			1L,
			"test1@example.com",
			"test1"
		);
	}

	public static AuthUser createAuthUserEntityForAuthor() {
		return AuthUser.create(
			1L,
			"author@example.com",
			"author"
		);
	}
}
