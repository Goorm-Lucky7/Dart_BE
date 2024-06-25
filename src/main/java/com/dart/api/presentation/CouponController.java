package com.dart.api.presentation;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dart.api.application.coupon.CouponService;
import com.dart.api.application.coupon.GeneralCouponManageService;
import com.dart.api.application.coupon.PriorityCouponManageService;
import com.dart.api.domain.auth.entity.AuthUser;
import com.dart.api.dto.coupon.request.GeneralCouponPublishDto;
import com.dart.api.dto.coupon.request.PriorityCouponPublishDto;
import com.dart.api.dto.coupon.response.CouponReadAllDto;
import com.dart.api.dto.coupon.response.MyCouponReadAllDto;
import com.dart.global.auth.annotation.Auth;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class CouponController {
	private final PriorityCouponManageService priorityCouponManageService;
	private final GeneralCouponManageService generalCouponManageService;
	private final CouponService couponService;

	@PostMapping("/priority-coupon")
	public void registerQueue(@RequestBody PriorityCouponPublishDto dto, @Auth AuthUser authUser) {
		priorityCouponManageService.registerQueue(dto, authUser.email());
	}

	@PostMapping("/general-coupon")
	public void publish(@RequestBody GeneralCouponPublishDto dto, @Auth AuthUser authUser) {
		generalCouponManageService.publish(dto, authUser);
	}

	@GetMapping
	public CouponReadAllDto readAll(@Auth(required = false) AuthUser authUser) {
		return couponService.readAll(authUser);
	}

	@GetMapping("/my-coupon")
	public MyCouponReadAllDto readMyCoupon(@Auth AuthUser authUser) {
		return couponService.readAllMyCoupon(authUser);
	}
}
