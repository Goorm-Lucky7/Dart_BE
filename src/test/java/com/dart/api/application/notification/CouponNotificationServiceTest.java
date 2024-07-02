package com.dart.api.application.notification;

import static org.mockito.BDDMockito.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dart.api.domain.coupon.entity.PriorityCoupon;
import com.dart.api.domain.coupon.repository.PriorityCouponRepository;
import com.dart.api.domain.notification.repository.SSESessionRepository;
import com.dart.api.dto.notification.response.NotificationReadDto;
import com.dart.support.CouponFixture;

@ExtendWith(MockitoExtension.class)
class CouponNotificationServiceTest {

	@Mock
	private SSESessionRepository sseSessionRepository;

	@Mock
	private PriorityCouponRepository priorityCouponRepository;

	@InjectMocks
	private CouponNotificationService couponNotificationService;

	@Test
	@DisplayName("SEND COUPON PUBLISH NOTIFICATION WITH COUPONS(⭕️ SUCCESS): 성공적으로 실시간 쿠폰 발행 알림을 모든 클라이언트들에게 전송했습니다.")
	void sendCouponPublishNotification_withCoupons_success() {
		// GIVEN
		List<PriorityCoupon> priorityCouponList = Arrays.asList(
			CouponFixture.createPriorityCoupon(),
			CouponFixture.createPriorityCoupon()
		);

		given(priorityCouponRepository.findByStartedAt(LocalDate.now())).willReturn(priorityCouponList);

		// WHEN
		couponNotificationService.sendCouponPublishNotification();

		// THEN
		verify(sseSessionRepository).sendEventToAll(any(NotificationReadDto.class));
		verify(priorityCouponRepository).findByStartedAt(LocalDate.now());
	}

	@Test
	@DisplayName("SEND COUPON PUBLISH NOTIFICATION NO COUPONS(⭕️ SUCCESS): 실시간 쿠폰이 존재하지 않아 실시간 쿠폰 발행 알림을 전송하지 않았습니다.")
	void sendCouponPublishNotification_noCoupons_success() {
		// GIVEN
		List<PriorityCoupon> priorityEmptyCouponList = Collections.emptyList();

		given(priorityCouponRepository.findByStartedAt(LocalDate.now())).willReturn(priorityEmptyCouponList);

		// WHEN
		couponNotificationService.sendCouponPublishNotification();

		// THEN
		verify(sseSessionRepository, never()).sendEventToAll(any(NotificationReadDto.class));
		verify(priorityCouponRepository).findByStartedAt(LocalDate.now());
	}
}
