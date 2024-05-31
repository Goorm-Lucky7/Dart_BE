package luckyseven.dart.api.domain.member.entity;

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
import luckyseven.dart.api.dto.member.request.MemberUpdateDto;
import luckyseven.dart.api.dto.member.request.SignUpDto;
import luckyseven.dart.global.common.entity.BaseTimeEntity;

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

	@Column(name = "bank", nullable = true)
	private String bank;

	@Column(name = "account", nullable = true)
	private String account;

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
		String bank,
		String account,
		String introduce
	) {
		this.email = email;
		this.nickname = nickname;
		this.password = password;
		this.birthday = birthday;
		this.bank = bank;
		this.account = account;
		this.introduce = introduce;
	}

	public static Member signup(SignUpDto signUpDto, String password) {
		return Member.builder()
			.email(signUpDto.email())
			.nickname(signUpDto.nickname())
			.password(password)
			.birthday(signUpDto.birthday())
			.bank(signUpDto.bank())
			.account(signUpDto.account())
			.introduce(signUpDto.introduce())
			.build();
	}

	public void updateMemberProfile(MemberUpdateDto memberUpdateDto, String password) {
		this.nickname = memberUpdateDto.nickname();
		this.password = password;
		this.bank = bank;
		this.account = account;
		this.introduce = introduce;
	}
}
