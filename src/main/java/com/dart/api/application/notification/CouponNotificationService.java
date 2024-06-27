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
import com.dart.api.domain.notification.repository.NotificationRepository;
import com.dart.api.domain.notification.repository.SSESessionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouponNotificationService {

	private final NotificationRepository notificationRepository;
	private final SSESessionRepository sseSessionRepository;
	private final PriorityCouponRepository priorityCouponRepository;

	@Scheduled(cron = DAILY_AT_MIDNIGHT)
	@Transactional
	public void sendCouponPublishNotification() {
		List<PriorityCoupon> priorityCouponList = getTodayCoupons();

		if (priorityCouponList.isEmpty()) {
			log.info("[✅ LOGGER] NO LIVE COUPONS STARTING TODAY");
			return;
		}

		final String couponTitles = priorityCouponList.get(0).getTitle();
		final String couponDetails = "LIVE COUPON '" + couponTitles + "' IS NOW AVAILABLE";

		sendCouponEventToAllNotification(couponDetails);
	}

	private List<PriorityCoupon> getTodayCoupons() {
		LocalDate startedAt = LocalDate.now();
		return priorityCouponRepository.findByStartedAt(startedAt);
	}

	private void sendCouponEventToAllNotification(String couponDetails) {
		sseSessionRepository.sendEventToAll(couponDetails, NotificationType.LIVE.getName());
		log.info("[✅ LOGGER] LIVE COUPON START NOTIFICATION SENT: {}", couponDetails);

		saveCommonNotification(couponDetails);
	}

	private void saveCommonNotification(String message) {
		if (notificationRepository.existsByNotificationType(NotificationType.LIVE)) {
			log.info("[✅ LOGGER] DUPLICATE LIVE COUPON START NOTIFICATION DETECTED: {}", message);
			return;
		}

		final Notification notification = Notification.createNotification(message, NotificationType.LIVE);
		notificationRepository.save(notification);
	}
}
