package com.dart.api.domain.auth.repository;

import static com.dart.global.common.util.RedisConstant.*;
import static org.apache.commons.lang3.BooleanUtils.*;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.dart.api.infrastructure.redis.HashRedisRepository;
import com.dart.api.infrastructure.redis.ValueRedisRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class EmailRedisRepository {

	private final ValueRedisRepository valueRedisRepository;
	private final HashRedisRepository hashRedisRepository;

	@Transactional
	public void setEmail(String to, String code) {
		Map<String, String> emailInfo = new HashMap<>();
		emailInfo.put(CODE, code);
		emailInfo.put(VERIFIED, FALSE);

			hashRedisRepository.saveHashEntriesWithExpiry(REDIS_EMAIL_PREFIX + to, emailInfo, EMAIL_VERIFICATION_EXPIRATION_TIME_SECONDS);
	}

	@Transactional(readOnly = true)
	public String getEmail(String key) {
		String value = valueRedisRepository.getValue(REDIS_EMAIL_PREFIX + key);
		return value != null ? value : "false";
	}

	public String findVerificationCodeByEmail(String to) {
		return hashRedisRepository.getHashEntry(REDIS_EMAIL_PREFIX + to, CODE);
	}

	@Transactional(readOnly = true)
		public boolean isVerified (String to){
			return TRUE.equals(hashRedisRepository.getHashEntry(REDIS_EMAIL_PREFIX + to, VERIFIED));
	}

	public void setVerified(String to) {
		hashRedisRepository.updateHashEntry(REDIS_EMAIL_PREFIX + to, VERIFIED, TRUE);
	}

	public void deleteEmail(String key) {
		valueRedisRepository.deleteValue(REDIS_EMAIL_PREFIX + key);
	}

	public boolean checkExistsEmail(String key) {
		return valueRedisRepository.isValueExists(REDIS_EMAIL_PREFIX + key);
	}
}
