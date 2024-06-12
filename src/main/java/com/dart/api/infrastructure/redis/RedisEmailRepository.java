package com.dart.api.infrastructure.redis;

import static com.dart.api.infrastructure.redis.RedisConstant.REDIS_EMAIL_PREFIX;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class RedisEmailRepository {

	@Value("${spring.mail.auth-code-expiration-millis}")
	private int authCodeExpirationMillis;

	private final RedisHashRepository redisHashRepository;

	public void setEmail(String key, String data) {
		redisHashRepository.setExpire(REDIS_EMAIL_PREFIX + key, data, authCodeExpirationMillis/1000);
	}

	@Transactional(readOnly = true)
	public String getEmail(String key) {
		String value = redisHashRepository.get(REDIS_EMAIL_PREFIX + key);
		return value != null ? value : "false";
	}

	public void deleteEmail(String key) {
		redisHashRepository.delete(REDIS_EMAIL_PREFIX + key);
	}

	public boolean checkExistsEmail(String key) {
		return redisHashRepository.exists(REDIS_EMAIL_PREFIX + key);
	}
}
