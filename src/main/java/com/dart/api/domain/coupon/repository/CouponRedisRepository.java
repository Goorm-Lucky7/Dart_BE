package com.dart.api.domain.coupon.repository;

import static com.dart.global.common.util.RedisConstant.*;

import java.util.Objects;

import org.springframework.stereotype.Repository;

import com.dart.api.infrastructure.redis.ZSetRedisRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CouponRedisRepository {
	private final ZSetRedisRepository zSetRedisRepository;

	public void addIfAbsentQueue(Long couponId, Long memberId, double registerTime) {
		zSetRedisRepository.addElementIfAbsent(
			REDIS_COUPON_PREFIX + couponId.toString(),
			memberId.toString(),
			registerTime,
			36000
		);
	}

	public boolean hasValue(Long couponId, Long memberId) {
		return Objects.nonNull(
			zSetRedisRepository.score(REDIS_COUPON_PREFIX + couponId.toString(), memberId.toString()));
	}

	public int sizeQueue(Long couponId) {
		return zSetRedisRepository
			.size(REDIS_COUPON_PREFIX + couponId.toString())
			.intValue();
	}
}
