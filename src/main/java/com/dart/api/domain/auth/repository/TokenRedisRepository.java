package com.dart.api.domain.auth.repository;

import static com.dart.global.common.util.RedisConstant.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.dart.api.infrastructure.redis.ValueRedisRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class TokenRedisRepository {

	private final ValueRedisRepository valueRedisRepository;

	public void saveAccessToken(String key, String data, long accessTokenExpire) {
		if (accessTokenExpire / 1000 >= 1.0) {
			valueRedisRepository.saveValueWithExpiry(REDIS_ACCESS_TOKEN_PREFIX + key, data, accessTokenExpire / 1000);
		}
	}

	public void saveBlacklistToken(String email, String token) {
		valueRedisRepository.saveValue(REDIS_BLACKLIST_TOKEN_PREFIX + email, token);
	}

	public String getAccessToken(String key) {
		return valueRedisRepository.getValue(REDIS_ACCESS_TOKEN_PREFIX + key);
	}

	public String getBlacklistToken(String email) {
		return valueRedisRepository.getValue(REDIS_BLACKLIST_TOKEN_PREFIX + email);
	}

	public void deleteAccessToken(String email) {
		valueRedisRepository.deleteValue(REDIS_ACCESS_TOKEN_PREFIX + email);
	}

	public void deleteBlacklistToken(String email) {
		valueRedisRepository.deleteValue(REDIS_BLACKLIST_TOKEN_PREFIX + email);
	}

	public boolean checkAccessTokenExists(String email) {
		return valueRedisRepository.isValueExists(REDIS_ACCESS_TOKEN_PREFIX + email);
	}
}
