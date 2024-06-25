package com.dart.api.dto.coupon.request;

import jakarta.validation.constraints.NotNull;

public record GeneralCouponPublishDto(
	@NotNull
	Long generalCouponId
) {
}
