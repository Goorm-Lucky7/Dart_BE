package com.dart.api.dto.coupon.response;

import java.time.LocalDate;

import lombok.Builder;

@Builder
public record PriorityCouponDetail(
	Long priorityCouponId,
	int stock,
	LocalDate startDate,
	LocalDate endDate,
	String title,
	int couponType,
	boolean isFinished
) {
}
