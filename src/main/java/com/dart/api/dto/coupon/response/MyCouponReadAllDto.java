package com.dart.api.dto.coupon.response;

import java.util.List;

public record MyCouponReadAllDto(
	List<MyCouponDetail> myCoupons
) {
}
