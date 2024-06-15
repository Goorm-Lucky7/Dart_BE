package com.dart.api.infrastructure.redis;

import static com.dart.global.common.util.GlobalConstant.*;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RedisConstant {
	public static final String TOKEN_PREFIX = "token:";
	public static final String EMAIL_PREFIX = "email:";
	public static final String NICKNAME_PREFIX = "nickname:";
	public static final String GALLERY_PREFIX = "gallery:";
	public static final String PAYMENT_PREFIX = "payment:";
	public static final String SESSION_EMAIL_PREFIX = "session-email:";
	public static final String SESSION_NICKNAME_PREFIX = "session-nickname:";

	public static final String CODE = "code";
	public static final String RESERVED = "reserved";
	public static final String VERIFIED = "verified";

	public static final long EMAIL_VERIFICATION_EXPIRATION_TIME_SECONDS = THIRTY_MINUTES;
	public static final long NICKNAME_VERIFICATION_EXPIRATION_TIME_SECONDS = THIRTY_MINUTES;
	public static final int SESSION_EMAIL_EXPIRATION_TIME_SECONDS = THIRTY_MINUTES;
	public static final int SESSION_NICKNAME_EXPIRATION_TIME_SECONDS = THIRTY_MINUTES;
}