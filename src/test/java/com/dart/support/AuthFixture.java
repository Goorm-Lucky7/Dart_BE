package com.dart.support;

import com.dart.api.domain.auth.entity.AuthUser;

public class AuthFixture {

	public static AuthUser createAuthUserEntity() {
		return AuthUser.create(1L, "test1@example.com", "test1");
	}
}
