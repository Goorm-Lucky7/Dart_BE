package com.dart.support;

import java.time.LocalDateTime;

import com.dart.api.domain.coupon.entity.Coupon;
import com.dart.api.domain.coupon.entity.CouponType;

public class CouponFixture {
	public static Coupon create() {
		return Coupon.builder()
			.stock(100)
			.name("coupon-test")
			.description("coupon-description")
			.durationAt(LocalDateTime.now().plusDays(1))
			.validAt(LocalDateTime.now().plusDays(2))
			.couponType(CouponType.TEN_PERCENT)
			.build();
	}
}
