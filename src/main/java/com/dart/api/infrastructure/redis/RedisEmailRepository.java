package com.dart.api.infrastructure.redis;

import static com.dart.api.infrastructure.redis.RedisConstant.*;
import static org.apache.commons.lang3.BooleanUtils.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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

	@Transactional
	public void setEmail(String to, String code) {
		Map<String, String> emailInfo = new HashMap<>();
		emailInfo.put(EMAIL_CODE, code);
		emailInfo.put(EMAIL_VERIFIED, FALSE);

		redisHashRepository.setHashOpsAndExpire(REDIS_EMAIL_PREFIX + to, emailInfo,
			TimeUnit.MILLISECONDS.toSeconds(authCodeExpirationMillis));
	}

	@Transactional(readOnly = true)
	public String findVerificationCodeByEmail(String to) {
		return redisHashRepository.getHashOps(REDIS_EMAIL_PREFIX + to, EMAIL_CODE);
	}

	@Transactional(readOnly = true)
	public boolean isVerified(String to) {
		return TRUE.equals(redisHashRepository.getHashOps(REDIS_EMAIL_PREFIX + to, EMAIL_VERIFIED));
	}

	public void setVerified(String to) {
		redisHashRepository.updateHashOpsField(REDIS_EMAIL_PREFIX + to, EMAIL_VERIFIED, TRUE);
	}

	public boolean existsEmail(String email) {
		return redisHashRepository.exists(REDIS_EMAIL_PREFIX + email);
	}

	public void deleteEmail(String email) {
		redisHashRepository.delete(REDIS_EMAIL_PREFIX + email);
	}
}
