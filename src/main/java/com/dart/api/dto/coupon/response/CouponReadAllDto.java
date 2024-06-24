package com.dart.api.dto.coupon.response;

import java.util.List;

public record CouponReadAllDto(
	List<PriorityCouponDetail> priorityCoupon,
	List<GeneralCouponDetail> generalCoupon
) {
}
