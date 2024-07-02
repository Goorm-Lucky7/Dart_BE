package com.dart.global.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PaymentConstant {
	public static final String READY_URL = "https://kapi.kakao.com/v1/payment/ready";
	public static final String APPROVE_URL = "https://kapi.kakao.com/v1/payment/approve";
	public static final String SUCCESS_URL = "https://dartgallery.site/api/payment/kakao/success";
	public static final String CANCEL_URL = "https://dartgallery.site/api/payment/kakao/cancel";
	public static final String FAIL_URL = "https://dartgallery.site/api/payment/kakao/fail";
	public static final String SUCCESS_REDIRECT_URL = "https://www.dartgallery.site/payment/success/";
	public static final String FAIL_REDIRECT_URL = "https://www.dartgallery.site/payment/fail";
	public static final String TAX = "0";
	public static final String CID = "TC0ONETIME";
	public static final String QUANTITY = "1";
	public static final long THIRTY_MINUTE = 300;
	public static final int FREE = 0;
}
