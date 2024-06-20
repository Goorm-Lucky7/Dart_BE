package com.dart.api.application.coupon;

import static com.dart.global.common.util.CouponConstant.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.Set;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dart.api.domain.coupon.entity.PriorityCoupon;
import com.dart.api.domain.coupon.entity.PriorityCouponWallet;
import com.dart.api.domain.coupon.repository.PriorityCouponRedisRepository;
import com.dart.api.domain.coupon.repository.PriorityCouponWalletRepository;
import com.dart.api.domain.member.entity.Member;
import com.dart.api.domain.member.repository.MemberRepository;
import com.dart.api.dto.prioritycoupon.request.PriorityCouponPublishDto;
import com.dart.global.error.exception.BadRequestException;
import com.dart.global.error.exception.ConflictException;
import com.dart.global.error.exception.NotFoundException;
import com.dart.global.error.model.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class PriorityCouponManageService {
	private final MemberRepository memberRepository;
	private final PriorityCouponWalletRepository priorityCouponWalletRepository;
	private final PriorityCouponRedisRepository priorityCouponRedisRepository;
	private final PriorityCouponCacheService priorityCouponCacheService;

	@Scheduled(fixedDelay = ONE_SECOND)
	public void publish() {
		final LocalDate nowDate = LocalDate.now();
		final Optional<PriorityCoupon> optionalCoupon = priorityCouponCacheService.getByStartAt(nowDate);

		if (optionalCoupon.isEmpty()) {
			return;
		}

		final PriorityCoupon priorityCoupon = optionalCoupon.get();
		final int maxCount = priorityCoupon.getStock();
		final int currentCount = priorityCouponRedisRepository.getCount(priorityCoupon.getId());

		if (maxCount <= currentCount) {
			return;
		}

		final Set<String> emails = priorityCouponRedisRepository
			.rangeQueue(priorityCoupon.getId(), currentCount, currentCount + TEM_PERSON);

		if (emails.isEmpty()) {
			return;
		}

		success(emails, priorityCoupon);
		priorityCouponRedisRepository.increase(priorityCoupon.getId(), emails.size());
	}

	private void success(Set<String> emails, PriorityCoupon priorityCoupon) {
		for (String email : emails) {
			final Member member = memberRepository.findByEmail(email)
				.orElseThrow(() -> new NotFoundException(ErrorCode.FAIL_MEMBER_NOT_FOUND));

			priorityCouponWalletRepository.save(PriorityCouponWallet.create(priorityCoupon, member));
		}
	}

	public void registerQueue(PriorityCouponPublishDto dto, String email) {
		final LocalDate nowDate = LocalDate.now();
		final LocalDateTime nowDateTime = LocalDateTime.now();
		final PriorityCoupon priorityCoupon = priorityCouponCacheService.getByIdAndStartAt(dto.priorityCouponId(),
			nowDate);
		final LocalDateTime endDateTime = priorityCoupon.getEndedAt().minusDays(1).atTime(LocalTime.MAX);
		final double registerTime = System.currentTimeMillis();
		final long expiredTime = Duration.between(nowDateTime, endDateTime).getSeconds();

		validateRegisterQueue(priorityCoupon, email);
		priorityCouponRedisRepository.addIfAbsentQueue(dto.priorityCouponId(), email, registerTime, expiredTime);
	}

	private void validateRegisterQueue(PriorityCoupon priorityCoupon, String email) {
		if (priorityCouponRedisRepository.hasValue(priorityCoupon.getId(), email)) {
			throw new ConflictException(ErrorCode.FAIL_COUPON_CONFLICT);
		}

		final int maxCount = priorityCoupon.getStock();
		final int sizeQueue = priorityCouponRedisRepository.sizeQueue(priorityCoupon.getId());

		if (maxCount <= sizeQueue) {
			throw new BadRequestException(ErrorCode.FAIL_INVALID_STOCK_END);
		}
	}
}
