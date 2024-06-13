package com.dart.api.infrastructure.redis;

import static com.dart.api.infrastructure.redis.RedisConstant.*;
import static com.dart.global.common.util.PaymentConstant.*;

import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class RedisPaymentRepository {

	private final RedisHashRepository redisHashRepository;

	public void deleteData(String key) {
		redisHashRepository.delete(REDIS_PAYMENT_PREFIX + key);
	}

	public void setData(String key, String value) {
		redisHashRepository.setExpire(REDIS_PAYMENT_PREFIX + key, value, THIRTY_MINUTE);
	}
}
