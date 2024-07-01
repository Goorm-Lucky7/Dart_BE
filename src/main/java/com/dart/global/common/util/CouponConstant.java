package com.dart.global.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CouponConstant {
	public static final int ONE_SECOND = 1000;
	public static final long TEN_PERSON = 10;
	public static final int ONE_PERSON = 1;
	public static final String EVERY_MONTH_FIRST_DAY = "0 0 0 1 * ?";
	public static final String SUCCESS_MESSAGE = "쿠폰 발급 완료되었습니다.";
	public static final String FAIL_NO_STOCK_MESSAGE = "쿠폰 재고가 없습니다.";
	public static final String FAIL_ALREADY_REQUEST_MESSAGE = "이미 발급 요청을 하셨습니다.";
}
