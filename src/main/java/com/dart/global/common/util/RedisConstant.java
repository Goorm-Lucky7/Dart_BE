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
	public static final String REDIS_TRIE_PREFIX = "trie:";

	public static final String REDIS_SESSION_EMAIL_PREFIX = "session-email:";
	public static final String REDIS_SESSION_NICKNAME_PREFIX = "session-nickname:";

	public static final String CODE = "code";
	public static final String RESERVED = "reserved";
	public static final String VERIFIED = "verified";
	public static final String IS_END_OF_WORD = "is-end-of-word";
	public static final String TITLE = "title";
	public static final String AUTHOR = "author";
	public static final String HASHTAG = "hashtag";

	public static final long EMAIL_VERIFICATION_EXPIRATION_TIME_SECONDS = THIRTY_MINUTES;
	public static final long NICKNAME_VERIFICATION_EXPIRATION_TIME_SECONDS = THIRTY_MINUTES;
	public static final int SESSION_EMAIL_EXPIRATION_TIME_SECONDS = THIRTY_MINUTES;
	public static final int SESSION_NICKNAME_EXPIRATION_TIME_SECONDS = THIRTY_MINUTES;

}
