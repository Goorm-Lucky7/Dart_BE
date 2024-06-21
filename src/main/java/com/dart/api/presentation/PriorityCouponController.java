package com.dart.api.presentation;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dart.api.application.coupon.PriorityCouponManageService;
import com.dart.api.domain.auth.entity.AuthUser;
import com.dart.api.dto.prioritycoupon.request.PriorityCouponPublishDto;
import com.dart.global.auth.annotation.Auth;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class PriorityCouponController {
	private final PriorityCouponManageService couponManageService;

	@PostMapping("/priority-coupon")
	public void registerQueue(@RequestBody PriorityCouponPublishDto dto, @Auth AuthUser authUser) {
		couponManageService.registerQueue(dto, authUser.email());
	}
}
