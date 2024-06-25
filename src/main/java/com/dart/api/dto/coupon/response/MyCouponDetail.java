package com.dart.api.dto.coupon.response;

import lombok.Builder;

@Builder
public record MyCouponDetail(
	Long couponId,
	String title,
	int couponType,
	boolean isPriority
) {
}
