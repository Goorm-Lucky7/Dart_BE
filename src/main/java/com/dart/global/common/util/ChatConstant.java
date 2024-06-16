package com.dart.global.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ChatConstant {

	public static final String WEBSOCKET_ENDPOINT = "/ws";
	public static final String SUBSCRIPTION_PREFIX = "/sub";
	public static final String APPLICATION_DESTINATION_PREFIX = "/pub";
	public static final String ALLOWED_ORIGIN_PATTERN = "*";
	public static final String CHAT_SESSION_USER = "authUser";
	public static final int FREE_MESSAGE_EXPIRY_SECONDS = 604800;

	public static final int MESSAGE_SIZE_LIMIT = 160 * 64 * 1024;
	public static final int SEND_TIME_LIMIT = 100 * 10000;
	public static final int SEND_BUFFER_SIZE_LIMIT = 3 * 512 * 1024;
}
