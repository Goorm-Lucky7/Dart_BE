package com.dart.api.domain.member.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.dart.dto.member.request.SignUpDto;
import com.dart.global.common.entity.BaseTimeEntity;

@Entity
@Getter
@Table(name = "tbl_member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "email", nullable = false)
	private String email;

	@Column(name = "nickname", nullable = false)
	private String nickname;

	@Column(name = "password", nullable = false)
	private String password;

	@Column(name = "birthday", nullable = true)
	private LocalDate birthday;

	@Column(name = "introduce", nullable = true)
	private String introduce;

	@Column(name = "bank", nullable = true)
	private String bank;

	@Column(name = "account", nullable = true)
	private String account;

	@Enumerated(EnumType.STRING)
	@Column(name = "oauth_provider", nullable = true)
	private OAuthProvider oauthProvider;

	@Builder
	private Member(
		String email,
		String nickname,
		String password,
		LocalDate birthday,
		String introduce,
		String bank,
		String account
	) {
		this.email = email;
		this.nickname = nickname;
		this.password = password;
		this.birthday = birthday;
		this.introduce = introduce;
		this.bank = bank;
		this.account = account;
	}

	public static Member signup(SignUpDto signUpDto, String password) {
		return Member.builder()
			.email(signUpDto.email())
			.nickname(signUpDto.nickname())
			.password(password)
			.birthday(signUpDto.birthday())
			.introduce(signUpDto.introduce())
			.bank(signUpDto.bank())
			.account(signUpDto.account())
			.build();
	}

}
