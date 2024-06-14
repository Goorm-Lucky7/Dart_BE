package com.dart.api.infrastructure.redis;

import static com.dart.global.common.util.RedisConstant.REDIS_EMAIL_PREFIX;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class EmailRedisRepository {

	@Value("${spring.mail.auth-code-expiration-millis}")
	private int authCodeExpirationMillis;

	private final ValueRedisRepository valueRedisRepository;

	public void setEmail(String key, String data) {
		valueRedisRepository.saveValueWithExpiry(REDIS_EMAIL_PREFIX + key, data, authCodeExpirationMillis/1000);
	}

	@Transactional(readOnly = true)
	public String getEmail(String key) {
		String value = valueRedisRepository.getValue(REDIS_EMAIL_PREFIX + key);
		return value != null ? value : "false";
	}

	public void deleteEmail(String key) {
		valueRedisRepository.deleteValue(REDIS_EMAIL_PREFIX + key);
	}

	public boolean checkExistsEmail(String key) {
		return valueRedisRepository.isValueExists(REDIS_EMAIL_PREFIX + key);
	}
}
