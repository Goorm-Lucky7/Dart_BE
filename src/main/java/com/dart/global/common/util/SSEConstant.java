package com.dart.global.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SSEConstant {

	public static final long SSE_DEFAULT_TIMEOUT = 60 * 60 * 1000L;
	public static final long SSE_CREATE_GALLERY_TIMEOUT = 60 * 1000L;
	public static final int EVENT_ID_COMPARISON_RESULT = 0;
	public static final String SSE_EMITTER_EVENT_NAME = "notification";
	public static final String DAILY_AT_MIDNIGHT = "0 0 0 * * ?";

	public static final String SSE_CONNECTION_SUCCESS_MESSAGE = "SSE에 성공적으로 연결되었습니다.";
	public static final String LIVE_COUPON_ISSUED_MESSAGE = "실시간 쿠폰이 발행되었습니다.";
}
