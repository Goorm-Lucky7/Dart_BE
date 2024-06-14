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

	private final HashRedisRepository hashRedisRepository;

	public void setEmail(String key, String data) {
		hashRedisRepository.setExpire(REDIS_EMAIL_PREFIX + key, data, authCodeExpirationMillis/1000);
	}

	@Transactional(readOnly = true)
	public String getEmail(String key) {
		String value = hashRedisRepository.get(REDIS_EMAIL_PREFIX + key);
		return value != null ? value : "false";
	}

	public void deleteEmail(String key) {
		hashRedisRepository.delete(REDIS_EMAIL_PREFIX + key);
	}

	public boolean checkExistsEmail(String key) {
		return hashRedisRepository.exists(REDIS_EMAIL_PREFIX + key);
	}
}
