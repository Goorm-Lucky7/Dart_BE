package com.dart.api.application.coupon;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.dart.api.domain.coupon.entity.PriorityCoupon;
import com.dart.api.domain.coupon.repository.PriorityCouponRepository;
import com.dart.global.error.exception.NotFoundException;
import com.dart.global.error.model.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "coupons")
public class PriorityCouponCacheService {
	private final PriorityCouponRepository priorityCouponRepository;

	@Cacheable(key = "#couponId + #now.toString()")
	public PriorityCoupon getByIdAndStartAt(Long couponId, LocalDate now) {
		return priorityCouponRepository.findCouponByIdAndDateRange(couponId, now)
			.orElseThrow(() -> new NotFoundException(ErrorCode.FAIL_COUPON_NOT_FOUND));
	}

	@Cacheable(key = "#now")
	public Optional<PriorityCoupon> getByStartAt(LocalDate now) {
		return priorityCouponRepository.findCouponByDateRange(now);
	}
}

