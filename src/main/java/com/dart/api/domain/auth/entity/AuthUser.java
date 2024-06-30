package com.dart.api.domain.auth.entity;

public record AuthUser(
	Long id,
	String email,
	String nickname
) {

	public static AuthUser create(Long id, String email, String nickname) {
		return new AuthUser(id, email, nickname);
	}
}
