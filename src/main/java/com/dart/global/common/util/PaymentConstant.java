package com.dart.global.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PaymentConstant {
	public static final String READY_URL = "https://kapi.kakao.com/v1/payment/ready";
	public static final String APPROVE_URL = "https://kapi.kakao.com/v1/payment/approve";
	public static final String SUCCESS_URL = "https://dartgallery.site/api/payment/success";
	public static final String CANCEL_URL = "https://dartgallery.site/api/payment/cancel";
	public static final String FAIL_URL = "https://dartgallery.site/api/payment/fail";
	public static final String PARTNER_USER = "USER";
	public static final String PARTNER_ORDER = "DART";
	public static final String TAX = "0";
	public static final String CID = "TC0ONETIME";
	public static final String QUANTITY = "1";
	public static final long THIRTY_MINUTE = 1800;
}
