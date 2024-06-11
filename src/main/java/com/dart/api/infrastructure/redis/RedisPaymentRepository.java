package com.dart.api.infrastructure.redis;

import static com.dart.api.infrastructure.redis.RedisConstant.REDIS_PAYMENT_PREFIX;

import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class RedisPaymentRepository {

	private final RedisHashRepository redisHashRepository;

	public void deletePayment(String key) {
		redisHashRepository.delete(REDIS_PAYMENT_PREFIX + key);
	}
}
