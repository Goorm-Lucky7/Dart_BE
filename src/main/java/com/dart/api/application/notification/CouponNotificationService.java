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
import com.dart.global.error.exception.NotFoundException;
import com.dart.global.error.model.ErrorCode;

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
			log.info("[✅ LOGGER] NO COUPONS STARTING TODAY");
			return;
		}

		final String couponTitles = priorityCouponList.get(0).getTitle();
		final String couponDetails = "COUPON '" + couponTitles + "' IS NOW AVAILABLE";

		sendCouponStartWithNotification(couponDetails);
	}

	private List<PriorityCoupon> getTodayCoupons() {
		LocalDate startedAt = LocalDate.now();
		return priorityCouponRepository.findByStartedAt(startedAt);
	}

	private void sendCouponStartWithNotification(String couponDetails) {
		sseSessionRepository.sendEventToAll(couponDetails, NotificationType.COUPON_START.name());
		log.info("[✅ LOGGER] COUPON START NOTIFICATION SENT: {}", couponDetails);

		saveCommonNotification(couponDetails);
	}

	private void saveCommonNotification(String message) {
		validateMessageAndNotificationTypeExists();

		final Notification notification = Notification.createNotification(
			message,
			NotificationType.COUPON_START,
			PRIORITY_COUPON_EVENT_URL
		);
		notificationRepository.save(notification);
	}

	private void validateMessageAndNotificationTypeExists() {
		if (!notificationRepository.existsByNotificationType(NotificationType.COUPON_START)) {
			throw new NotFoundException(ErrorCode.FAIL_NOTIFICATION_NOT_FOUND);
		}
	}
}
