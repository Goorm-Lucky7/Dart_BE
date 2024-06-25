package com.dart.api.dto.coupon.response;

import lombok.Builder;

@Builder
public record GeneralCouponDetail(
	Long generalCouponId,
	String title,
	int couponType,
	boolean hasCoupon
) {
}
