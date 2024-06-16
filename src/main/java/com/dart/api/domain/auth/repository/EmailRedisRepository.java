package com.dart.api.domain.auth.repository;

import static com.dart.api.infrastructure.redis.RedisConstant.REDIS_EMAIL_PREFIX;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.dart.api.infrastructure.redis.ValueRedisRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class EmailRedisRepository {

	@Value("${spring.mail.auth-code-expiration-millis}")
	private int authCodeExpirationMillis;

	private final ValueRedisRepository valueRedisRepository;

	@Transactional
	public void setEmail(String to, String code) {
		Map<String, String> emailInfo = new HashMap<>();
		emailInfo.put(CODE, code);
		emailInfo.put(VERIFIED, FALSE);

			valueRedisRepository.saveValueWithExpiry(EMAIL_PREFIX + to, emailInfo, EMAIL_VERIFICATION_EXPIRATION_TIME_SECONDS);
	}

	@Transactional(readOnly = true)
	public String getEmail(String key) {
		String value = valueRedisRepository.getValue(REDIS_EMAIL_PREFIX + key);
		return value != null ? value : "false";
	public String findVerificationCodeByEmail(String to) {
		return redisHashRepository.getHashOps(EMAIL_PREFIX + to, CODE);
	}

	@Transactional(readOnly = true)
	public boolean isVerified(String to) {
		return TRUE.equals(redisHashRepository.getHashOps(EMAIL_PREFIX + to, VERIFIED));
	}

	public void setVerified(String to) {
		redisHashRepository.updateHashOpsField(EMAIL_PREFIX + to, VERIFIED, TRUE);
	}

	public void deleteEmail(String key) {
		valueRedisRepository.deleteValue(REDIS_EMAIL_PREFIX + key);
	}

	public boolean checkExistsEmail(String key) {
		return valueRedisRepository.isValueExists(REDIS_EMAIL_PREFIX + key);
	}
		public void deleteEmail(String email) {
		redisHashRepository.delete(EMAIL_PREFIX + email);
	}
}
