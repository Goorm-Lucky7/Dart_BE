package com.dart.global.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AuthConstant {

	public static final String ACCESS_TOKEN_HEADER = "Authorization";
	public static final String REFRESH_TOKEN_COOKIE_NAME = "Refresh-Token";
	public static final String BEARER = "Bearer";
	public static final String EMAIL_TITLE = "Dart 인증 이메일";
	public static final int EMAIL_CODE_LENGTH = 6;
}
