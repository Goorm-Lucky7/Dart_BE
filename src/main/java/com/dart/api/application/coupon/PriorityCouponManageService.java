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
import com.dart.api.domain.member.entity.Member;
import com.dart.api.domain.member.repository.MemberRepository;
import com.dart.api.dto.coupon.request.PriorityCouponPublishDto;
import com.dart.global.common.util.ClockHolder;
import com.dart.global.error.exception.ConflictException;
import com.dart.global.error.exception.NotFoundException;
import com.dart.global.error.model.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PriorityCouponManageService {
	private final MemberRepository memberRepository;
	private final PriorityCouponWalletRepository priorityCouponWalletRepository;
	private final PriorityCouponRedisRepository priorityCouponRedisRepository;
	private final PriorityCouponCacheService priorityCouponCacheService;
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
		final int currentCount = priorityCouponRedisRepository.getCount(priorityCoupon.getId());

		final Set<Long> membersId = priorityCouponRedisRepository
			.rangeQueue(priorityCoupon.getId(), currentCount, currentCount + TEN_PERSON);

		if (membersId.isEmpty()) {
			return;
		}

		for (Long memberId : membersId) {
			int rank = priorityCouponRedisRepository.rankQueue(couponId, memberId);

			if (maxCount <= rank) {
				log.info("재고 부족");
				priorityCouponRedisRepository.increase(priorityCoupon.getId(), 1);
				continue;
			}

			final Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new NotFoundException(ErrorCode.FAIL_MEMBER_NOT_FOUND));

			priorityCouponWalletRepository.save(PriorityCouponWallet.create(priorityCoupon, member));
			priorityCouponRedisRepository.increase(priorityCoupon.getId(), 1);
			log.info("이벤트 성공");
		}
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
			log.info("이미 발급 요청");
			throw new ConflictException(ErrorCode.FAIL_COUPON_CONFLICT);
		}
	}
}
