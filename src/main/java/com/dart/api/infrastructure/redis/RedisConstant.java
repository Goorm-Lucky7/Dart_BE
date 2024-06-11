package com.dart.api.infrastructure.redis;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RedisConstant {
	public static final String REDIS_TOKEN_PREFIX = "token:";
	public static final String REDIS_EMAIL_PREFIX = "email:";
	public static final String REDIS_GALLERY_PREFIX = "gallery:";
	public static final String REDIS_PAYMENT_PREFIX = "payment:";
}