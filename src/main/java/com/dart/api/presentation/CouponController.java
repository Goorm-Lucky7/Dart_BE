package com.dart.api.presentation;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dart.api.application.coupon.CouponManageService;
import com.dart.api.domain.auth.entity.AuthUser;
import com.dart.global.auth.annotation.Auth;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor

public class CouponController {
	private final CouponManageService couponManageService;

	@PostMapping("/{coupon-id}")
	public void registerQueue(@PathVariable("coupon-id") Long couponId, @Auth AuthUser authUser) {
		couponManageService.registerQueue(couponId, authUser);
	}
}
