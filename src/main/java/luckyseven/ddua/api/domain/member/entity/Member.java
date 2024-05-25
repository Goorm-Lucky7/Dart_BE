package luckyseven.ddua.api.domain.member.entity;

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
import luckyseven.ddua.api.global.common.BaseTimeEntity;

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

	@Column(name = "age", nullable = true)
	private int age;

	@Column(name = "introduce", nullable = true)
	private String introduce;

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
		int age,
		String introduce,
		String account
	) {
		this.email = email;
		this.nickname = nickname;
		this.password = password;
		this.age = age;
		this.introduce = introduce;
		this.account = account;
	}
}
