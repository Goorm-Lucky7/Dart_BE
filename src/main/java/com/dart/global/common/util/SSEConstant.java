package com.dart.global.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SSEConstant {

	public static final long SSE_DEFAULT_TIMEOUT = 60 * 60 * 1000L;
	public static final long SSE_CREATE_GALLERY_TIMEOUT = 60 * 1000L;
	public static final int EVENT_ID_COMPARISON_RESULT = 0;
	public static final int REEXHIBITION_REQUEST_COUNT = 10;
	public static final String SSE_EMITTER_EVENT_NAME = "notification";
	public static final String DAILY_AT_MIDNIGHT = "0 0 0 * * ?";

	public static final String SSE_CONNECTION_SUCCESS_MESSAGE = "SSE에 성공적으로 연결되었습니다.";
	public static final String LIVE_COUPON_ISSUED_MESSAGE = "실시간 쿠폰이 발행되었습니다.";
	public static final String REEXHIBITION_REQUEST_MESSAGE = "재전시 요청이 10회를 초과했습니다. 새로운 재전시 요청이 들어왔으니 확인해 주세요.";
}
