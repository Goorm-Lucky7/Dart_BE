package com.dart.api.application.coupon;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dart.api.domain.coupon.entity.Coupon;
import com.dart.api.domain.coupon.repository.CouponRedisRepository;
import com.dart.api.domain.coupon.repository.CouponRepository;
import com.dart.global.error.exception.BadRequestException;
import com.dart.global.error.exception.ConflictException;
import com.dart.support.CouponFixture;

@ExtendWith({MockitoExtension.class})
class CouponManageServiceTest {
	@Mock
	private CouponRepository couponRepository;

	@Mock
	private CouponRedisRepository couponRedisRepository;

	@InjectMocks
	private CouponManageService couponManageService;

	@Test
	@DisplayName("쿠폰 발급 요청을 성공적으로 대기열 큐에 등록한다. - Void")
	void registerQueue_Success() {
		// GIVEN
		Coupon coupon = CouponFixture.create();
		String testEmail = "test@example.com";

		given(couponRepository.findCouponByIdAndDateRange(eq(coupon.getId()), any(LocalDateTime.class))).willReturn(
			Optional.of(coupon));
		given(couponRedisRepository.hasValue(eq(coupon.getId()), eq(testEmail))).willReturn(false);
		given(couponRedisRepository.sizeQueue(eq(coupon.getId()))).willReturn(coupon.getStock() - 1);

		// WHEN
		couponManageService.registerQueue(coupon.getId(), testEmail);

		// THEN
		verify(couponRedisRepository)
			.addIfAbsentQueue(eq(coupon.getId()), eq(testEmail), anyDouble(), anyLong());
	}

	@Test
	@DisplayName("이미 해당 쿠폰은 발급받은 쿠폰이다. - ConflictException")
	void registerQueue_No_ConflictException() {
		// GIVEN
		Coupon coupon = CouponFixture.create();
		String testEmail = "test@example.com";

		given(couponRepository.findCouponByIdAndDateRange(eq(coupon.getId()), any(LocalDateTime.class))).willReturn(
			Optional.of(coupon));
		given(couponRedisRepository.hasValue(eq(coupon.getId()), eq(testEmail))).willReturn(true);

		// When & Then
		assertThatThrownBy(() -> couponManageService.registerQueue(coupon.getId(), testEmail))
			.isInstanceOf(ConflictException.class);
	}

	@Test
	@DisplayName("해당 쿠폰은 재고가 마감된 쿠폰이다. - BadRequestException")
	void registerQueue_No_BadRequestException() {
		// GIVEN
		Coupon coupon = CouponFixture.create();
		String testEmail = "test@example.com";

		given(couponRepository.findCouponByIdAndDateRange(eq(coupon.getId()), any(LocalDateTime.class))).willReturn(
			Optional.of(coupon));
		given(couponRedisRepository.hasValue(eq(coupon.getId()), eq(testEmail))).willReturn(false);
		given(couponRedisRepository.sizeQueue(eq(coupon.getId()))).willReturn(coupon.getStock());

		// When & Then
		assertThatThrownBy(() -> couponManageService.registerQueue(coupon.getId(), testEmail))
			.isInstanceOf(BadRequestException.class);
	}
}

