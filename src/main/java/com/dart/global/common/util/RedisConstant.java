package com.dart.global.common.util;

import static com.dart.global.common.util.GlobalConstant.*;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RedisConstant {
	public static final String REDIS_TOKEN_PREFIX = "token:";
	public static final String REDIS_EMAIL_PREFIX = "email:";
	public static final String REDIS_GALLERY_PREFIX = "gallery:";
	public static final String REDIS_PAYMENT_PREFIX = "payment:";
	public static final String REDIS_CHAT_MESSAGE_PREFIX = "chatMessage:";
	public static final String REDIS_NICKNAME_PREFIX = "nickname:";
	public static final String REDIS_COUPON_PREFIX = "coupon:";
	public static final String REDIS_COUPON_COUNT_PREFIX = "coupon_count:";

	public static final String REDIS_SESSION_EMAIL_PREFIX = "session-email:";
	public static final String REDIS_SESSION_NICKNAME_PREFIX = "session-nickname:";

	public static final long FREE_EXHIBITION_MESSAGE_EXPIRY_DAYS = 30;
	public final static long ZSET_START_INDEX = 0;
	public final static long ZSET_END_INDEX_ALL = -1;

	public static final String CODE = "code";
	public static final String RESERVED = "reserved";
	public static final String VERIFIED = "verified";

	public static final long EMAIL_VERIFICATION_EXPIRATION_TIME_SECONDS = THIRTY_MINUTES;
	public static final long NICKNAME_VERIFICATION_EXPIRATION_TIME_SECONDS = THIRTY_MINUTES;
	public static final int SESSION_EMAIL_EXPIRATION_TIME_SECONDS = THIRTY_MINUTES;
	public static final int SESSION_NICKNAME_EXPIRATION_TIME_SECONDS = THIRTY_MINUTES;
}
