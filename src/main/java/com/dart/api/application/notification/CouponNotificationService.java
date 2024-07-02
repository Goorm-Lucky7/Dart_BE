package com.dart.api.application.notification;

import static com.dart.global.common.util.SSEConstant.*;

import java.time.LocalDate;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dart.api.domain.coupon.entity.PriorityCoupon;
import com.dart.api.domain.coupon.repository.PriorityCouponRepository;
import com.dart.api.domain.notification.entity.Notification;
import com.dart.api.domain.notification.entity.NotificationType;
import com.dart.api.domain.notification.repository.SSESessionRepository;
import com.dart.api.dto.notification.response.NotificationReadDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouponNotificationService {

	private final SSESessionRepository sseSessionRepository;
	private final PriorityCouponRepository priorityCouponRepository;

	@Scheduled(cron = DAILY_AT_MIDNIGHT)
	@Transactional
	public void sendCouponPublishNotification() {
		final List<PriorityCoupon> priorityCouponList = getTodayCoupons();

		if (priorityCouponList.isEmpty()) {
			log.info("[✅ LOGGER] NO LIVE COUPONS STARTING TODAY");
			return;
		}

		notifyAllClientsAboutNewCoupons();
	}

	private List<PriorityCoupon> getTodayCoupons() {
		LocalDate startedAt = LocalDate.now();
		return priorityCouponRepository.findByStartedAt(startedAt);
	}

	private void notifyAllClientsAboutNewCoupons() {
		final NotificationReadDto notificationReadDto = Notification.createNotificationReadDto(
			LIVE_COUPON_ISSUED_MESSAGE, NotificationType.LIVE.getName()
		);

		sseSessionRepository.sendEventToAll(notificationReadDto);
		log.info("[✅ LOGGER] LIVE COUPON START NOTIFICATION SENT: {}", notificationReadDto.message());
	}
}
