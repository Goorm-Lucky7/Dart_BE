package com.dart.global.common.util;

import static com.dart.global.common.util.GlobalConstant.*;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AuthConstant {

	public static final String ACCESS_TOKEN_HEADER = "Authorization";
	public static final String BEARER = "Bearer";
	public static final String REFRESH_TOKEN_COOKIE_NAME = "Refresh-Token";
	public static final String SESSION_ID = "Session-Id";

	public static final String EMAIL_TITLE = "Dart 인증 이메일";
	public static final int EMAIL_CODE_LENGTH = 6;

	public static long REFRESH_TOKEN_EXPIRATION_TIME_SECONDS = THIRTY_DAYS;
	public static long EMAIL_EXPIRATION_TIME_SECONDS = THIRTY_MINUTES;
	public static final int SESSION_EXPIRATION_TIME_SECONDS = THIRTY_MINUTES;


	@Component
	public static class PropertyLoader {

		@Value("${jwt.refresh-expire}")
		public void setRefreshTokenExpire(long refreshTokenExpire) {
			REFRESH_TOKEN_EXPIRATION_TIME_SECONDS = TimeUnit.MILLISECONDS.toSeconds(refreshTokenExpire);
		}

		@Value("${spring.mail.auth-code-expiration-millis}")
		public void setAuthCodeExpirationMillis(long authCodeExpirationMillis) {
			EMAIL_EXPIRATION_TIME_SECONDS = TimeUnit.MILLISECONDS.toSeconds(authCodeExpirationMillis);
		}
	}
}
