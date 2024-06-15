package com.dart.api.infrastructure.redis;

import static com.dart.api.infrastructure.redis.RedisConstant.*;
import static com.dart.global.common.util.AuthConstant.*;
import static org.apache.commons.lang3.BooleanUtils.*;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class RedisEmailRepository {

	private final RedisHashRepository redisHashRepository;

	@Transactional
	public void setEmail(String to, String code) {
		Map<String, String> emailInfo = new HashMap<>();
		emailInfo.put(CODE, code);
		emailInfo.put(VERIFIED, FALSE);

		redisHashRepository.setHashOpsAndExpire(EMAIL_PREFIX + to, emailInfo, EMAIL_VERIFICATION_EXPIRATION_TIME_SECONDS);
	}

	@Transactional(readOnly = true)
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

	public boolean existsEmail(String email) {
		return redisHashRepository.exists(EMAIL_PREFIX + email);
	}

	public void deleteEmail(String email) {
		redisHashRepository.delete(EMAIL_PREFIX + email);
	}
}
