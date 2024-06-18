package com.dart.api.application.coupon;

import static com.dart.global.common.util.CouponConstant.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dart.api.domain.auth.entity.AuthUser;
import com.dart.api.domain.coupon.entity.Coupon;
import com.dart.api.domain.coupon.entity.CouponWallet;
import com.dart.api.domain.coupon.repository.CouponRedisRepository;
import com.dart.api.domain.coupon.repository.CouponRepository;
import com.dart.api.domain.coupon.repository.CouponWalletRepository;
import com.dart.api.domain.member.entity.Member;
import com.dart.api.domain.member.repository.MemberRepository;
import com.dart.global.error.exception.BadRequestException;
import com.dart.global.error.exception.ConflictException;
import com.dart.global.error.exception.NotFoundException;
import com.dart.global.error.model.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class CouponManageService {
	private final MemberRepository memberRepository;
	private final CouponRepository couponRepository;
	private final CouponWalletRepository couponWalletRepository;
	private final CouponRedisRepository couponRedisRepository;

	@Scheduled(fixedDelay = ONE_SECOND)
	public void publish() {
		final LocalDateTime nowDatetime = LocalDateTime.now();
		final Optional<Coupon> optionalCoupon = couponRepository.findCouponByDateRange(nowDatetime);

		if (optionalCoupon.isEmpty()) {
			return;
		}

		final Coupon coupon = optionalCoupon.get();
		final int maxCount = coupon.getStock();
		final int currentCount = couponRedisRepository.getCount(coupon.getId());

		if (maxCount <= currentCount) {
			return;
		}

		final Set<String> emails = couponRedisRepository
			.rangeQueue(coupon.getId(), currentCount, currentCount + TEM_PERSON);

		success(emails, coupon);
		couponRedisRepository.increase(coupon.getId(), emails.size());
	}

	private void success(Set<String> emails, Coupon coupon) {
		for (String email : emails) {
			final Member member = memberRepository.findByEmail(email)
				.orElseThrow(() -> new NotFoundException(ErrorCode.FAIL_MEMBER_NOT_FOUND));

			couponWalletRepository.save(CouponWallet.create(coupon, member));
		}
	}

	public void registerQueue(Long couponId, AuthUser authUser) {
		final LocalDateTime nowDatetime = LocalDateTime.now();
		final Coupon coupon = couponRepository.findCouponByIdAndDateRange(couponId, nowDatetime)
			.orElseThrow(() -> new NotFoundException(ErrorCode.FAIL_COUPON_NOT_FOUND));
		final double registerTime = System.currentTimeMillis();
		final long expiredTime = Duration.between(nowDatetime, coupon.getDurationAt()).getSeconds();

		validateRegisterQueue(coupon, authUser.email());

		couponRedisRepository.addIfAbsentQueue(couponId, authUser.email(), registerTime, expiredTime);
	}

	private void validateRegisterQueue(Coupon coupon, String email) {
		if (couponRedisRepository.hasValue(coupon.getId(), email)) {
			throw new ConflictException(ErrorCode.FAIL_COUPON_CONFLICT);
		}

		final int maxCount = coupon.getStock();
		final int sizeQueue = couponRedisRepository.sizeQueue(coupon.getId());

		if (maxCount <= sizeQueue) {
			throw new BadRequestException(ErrorCode.FAIL_INVALID_STOCK_END);
		}
	}
}
