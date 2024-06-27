package com.dart.api.domain.coupon.repository;

import static com.dart.global.common.util.RedisConstant.*;
import static java.util.Objects.*;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.dart.api.infrastructure.redis.ValueRedisRepository;
import com.dart.api.infrastructure.redis.ZSetRedisRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PriorityCouponRedisRepository {
	private final ZSetRedisRepository zSetRedisRepository;
	private final ValueRedisRepository valueRedisRepository;

	public void addIfAbsentQueue(Long couponId, Long memberId, double registerTime, long expiredTime) {
		zSetRedisRepository.addElementWithExpiry(
			REDIS_COUPON_PREFIX + couponId.toString(),
			memberId,
			registerTime,
			expiredTime
		);
	}

	public boolean hasValue(Long couponId, Long memberId) {
		return Objects.nonNull(
			zSetRedisRepository.score(REDIS_COUPON_PREFIX + couponId.toString(), memberId));
	}

	public int sizeQueue(Long couponId) {
		return zSetRedisRepository
			.size(REDIS_COUPON_PREFIX + couponId.toString())
			.intValue();
	}

	public int rankQueue(Long couponId, Long memberId) {
		return zSetRedisRepository
			.rank(REDIS_COUPON_PREFIX + couponId.toString(), requireNonNull(memberId))
			.intValue();
	}

	public int getCount(Long couponId) {
		String couponCountKey = REDIS_COUPON_COUNT_PREFIX + couponId.toString();
		String count = valueRedisRepository.getValue(couponCountKey);

		if (isNull(count)) {
			return 0;
		}

		return Integer.parseInt(count);
	}

	public Set<Long> rangeQueue(Long couponId, long start, long end) {
		return zSetRedisRepository
			.getRange(REDIS_COUPON_PREFIX + couponId.toString(), start, end)
			.stream()
			.map(memberId -> Long.parseLong(String.valueOf(memberId)))
			.collect(Collectors.toSet());
	}

	public void increase(Long couponId, long count) {
		final String couponCountKey = REDIS_COUPON_COUNT_PREFIX + couponId;
		valueRedisRepository.increment(couponCountKey, count);
	}
}
