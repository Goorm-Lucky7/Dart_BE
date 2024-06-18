package com.dart.api.domain.coupon.repository;

import static com.dart.global.common.util.CouponConstant.*;
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
public class CouponRedisRepository {
	private final ZSetRedisRepository zSetRedisRepository;
	private final ValueRedisRepository valueRedisRepository;

	public void addIfAbsentQueue(Long couponId, String email, double registerTime) {
		zSetRedisRepository.addElementIfAbsent(
			REDIS_COUPON_PREFIX + couponId.toString(),
			email,
			registerTime,
			TWO_DAYS
		);
	}

	public boolean hasValue(Long couponId, String email) {
		return Objects.nonNull(
			zSetRedisRepository.score(REDIS_COUPON_PREFIX + couponId.toString(), email));
	}

	public int sizeQueue(Long couponId) {
		return zSetRedisRepository
			.size(REDIS_COUPON_PREFIX + couponId.toString())
			.intValue();
	}

	public int getCount(Long couponId) {
		String couponCountKey = REDIS_COUPON_PREFIX + couponId.toString();
		String count = valueRedisRepository.getValue(couponCountKey);

		if (isNull(count)) {
			return 0;
		}

		return Integer.parseInt(count);
	}

	public Set<String> rangeQueue(Long couponId, long start, long end) {
		return zSetRedisRepository
			.getRange(REDIS_COUPON_PREFIX + couponId.toString(), start, end)
			.stream()
			.map(String.class::cast)
			.collect(Collectors.toSet());
	}

	public void increase(Long couponId, long count) {
		final String couponCountKey = REDIS_COUPON_COUNT_PREFIX + couponId;
		valueRedisRepository.increment(couponCountKey, count);
	}
}
