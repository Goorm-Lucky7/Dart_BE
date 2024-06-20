package com.dart.api.dto.prioritycoupon.request;

import jakarta.validation.constraints.NotNull;

public record PriorityCouponPublishDto(
	@NotNull
	Long priorityCouponId
) {
}
