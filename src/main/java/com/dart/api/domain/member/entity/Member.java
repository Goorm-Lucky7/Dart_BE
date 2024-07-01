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
import com.dart.api.dto.member.request.SignUpDto;
import com.dart.api.dto.member.request.MemberUpdateDto;
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

	@Column(name = "email", unique = true, nullable = false)
	private String email;

	@Column(name = "nickname", unique = true, nullable = false)
	private String nickname;

	@Column(name = "password", nullable = false)
	private String password;

	@Column(name = "profile_image_url", nullable = true)
	private String profileImageUrl;

	@Column(name = "birthday", nullable = true)
	private LocalDate birthday;

	@Column(name = "introduce", nullable = true)
	private String introduce;

	@Enumerated(EnumType.STRING)
	@Column(name = "oauth_provider", nullable = true)
	private OAuthProvider oauthProvider;

	@Builder
	private Member(
		String email,
		String nickname,
		String password,
		LocalDate birthday,
		String introduce
	) {
		this.email = email;
		this.nickname = nickname;
		this.password = password;
		this.birthday = birthday;
		this.introduce = introduce;
	}

	public static Member signup(SignUpDto signUpDto, String password) {
		return Member.builder()
			.email(signUpDto.email())
			.nickname(signUpDto.nickname())
			.password(password)
			.birthday(signUpDto.birthday())
			.introduce(signUpDto.introduce())
			.build();
	}

	public void updateProfile(MemberUpdateDto memberUpdateDto, String profileImageUrl) {
		this.nickname = memberUpdateDto.nickname();
		this.profileImageUrl = profileImageUrl;
		this.introduce = memberUpdateDto.introduce();
	}
}
