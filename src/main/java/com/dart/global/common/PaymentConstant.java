package com.dart.global.common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PaymentConstant {
	public static final String READY_URL = "https://kapi.kakao.com/v1/payment/ready";
	public static final String APPROVE_URL = "https://kapi.kakao.com/v1/payment/approve";
	public static final String SUCCESS_URL = "http://localhost:8080/api/pay/success";
	public static final String CANCEL_URL = "http://localhost:8080/api/pay/cancel";
	public static final String FAIL_URL = "http://localhost:8080/api/pay/fail";
	public static final String PARTNER_USER = "USER";
	public static final String PARTNER_ORDER = "DART";
	public static final String TAX = "0";
	public static final String CID = "TC0ONETIME";
	public static final String QUANTITY = "1";
}
