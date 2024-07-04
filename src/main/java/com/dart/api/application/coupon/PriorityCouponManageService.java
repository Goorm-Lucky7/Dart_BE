package com.dart.api.application.coupon;

import static com.dart.global.common.util.CouponConstant.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dart.api.domain.coupon.entity.PriorityCoupon;
import com.dart.api.domain.coupon.entity.PriorityCouponWallet;
import com.dart.api.domain.coupon.repository.PriorityCouponRedisRepository;
import com.dart.api.domain.coupon.repository.PriorityCouponWalletRepository;
import com.dart.api.domain.notification.entity.Notification;
import com.dart.api.domain.notification.entity.NotificationType;
import com.dart.api.domain.notification.repository.SSESessionRepository;
import com.dart.api.dto.coupon.request.PriorityCouponPublishDto;
import com.dart.api.dto.notification.response.NotificationReadDto;
import com.dart.global.common.util.ClockHolder;
import com.dart.global.error.exception.ConflictException;
import com.dart.global.error.model.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PriorityCouponManageService {
	private final PriorityCouponWalletRepository priorityCouponWalletRepository;
	private final PriorityCouponRedisRepository priorityCouponRedisRepository;
	private final PriorityCouponCacheService priorityCouponCacheService;
	private final SSESessionRepository sseSessionRepository;
	private final ClockHolder clockHolder;

	@Scheduled(fixedDelay = ONE_SECOND)
	public void publish() {
		final LocalDate nowDate = LocalDate.now();
		final Optional<PriorityCoupon> optionalCoupon = priorityCouponCacheService.getByStartAt(nowDate);

		if (optionalCoupon.isEmpty()) {
			return;
		}

		final PriorityCoupon priorityCoupon = optionalCoupon.get();
		final int maxCount = priorityCoupon.getStock();
		final Long couponId = priorityCoupon.getId();
		final long currentCount = priorityCouponRedisRepository.getCount(priorityCoupon.getId());

		final Set<Long> membersId = priorityCouponRedisRepository
			.rangeQueue(priorityCoupon.getId(), currentCount, currentCount + TEN_PERSON);

		if (membersId.isEmpty()) {
			return;
		}

		decideEvent(membersId, couponId, maxCount, priorityCoupon);
	}

	private void decideEvent(Set<Long> membersId, Long couponId, int maxCount, PriorityCoupon priorityCoupon) {
		for (Long memberId : membersId) {
			final int rank = priorityCouponRedisRepository.rankQueue(couponId, memberId);

			if (maxCount <= rank) {
				notification(memberId, FAIL_NO_STOCK_MESSAGE);
				continue;
			}

			priorityCouponWalletRepository.save(PriorityCouponWallet.create(priorityCoupon, memberId));
			notification(memberId, SUCCESS_MESSAGE);
		}

		priorityCouponRedisRepository.increase(priorityCoupon.getId(), membersId.size());
	}

	public void registerQueue(PriorityCouponPublishDto dto, Long memberId) {
		final LocalDate nowDate = LocalDate.now();
		final LocalDateTime nowDateTime = LocalDateTime.now();
		final PriorityCoupon priorityCoupon = priorityCouponCacheService.getByIdAndStartAt(dto.priorityCouponId(),
			nowDate);
		final LocalDateTime endDateTime = clockHolder.minusOneDaysAtTime(priorityCoupon.getEndedAt());
		final double registerTime = System.currentTimeMillis();
		final long expiredTime = Duration.between(nowDateTime, endDateTime).getSeconds();

		validateRegisterQueue(priorityCoupon, memberId);
		priorityCouponRedisRepository.addIfAbsentQueue(dto.priorityCouponId(), memberId, registerTime, expiredTime);
	}

	private void validateRegisterQueue(PriorityCoupon priorityCoupon, Long memberId) {
		if (priorityCouponRedisRepository.hasValue(priorityCoupon.getId(), memberId)) {
			notification(memberId, FAIL_ALREADY_REQUEST_MESSAGE);
			throw new ConflictException(ErrorCode.FAIL_COUPON_CONFLICT);
		}
	}

	private void notification(Long memberId, String message) {
		NotificationReadDto notificationReadDto = Notification.createNotificationReadDto(
			message, NotificationType.COUPON.getName()
		);
		sseSessionRepository.sendEvent(memberId, notificationReadDto);
	}
}
