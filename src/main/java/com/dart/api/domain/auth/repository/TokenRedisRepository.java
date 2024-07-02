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
		valueRedisRepository.saveValueWithExpiry(REDIS_ACCESS_TOKEN_PREFIX + key, data, accessTokenExpire /1000);
	}

	@Transactional(readOnly = true)
	public String getAccessToken(String key) {
		String value = valueRedisRepository.getValue(REDIS_ACCESS_TOKEN_PREFIX + key);
		return value != null ? value : "false";
	}

	public void deleteAccessToken(String key) {
		valueRedisRepository.deleteValue(REDIS_ACCESS_TOKEN_PREFIX + key);
	}

	public void addToBlacklist(String token, long ttl) {
		valueRedisRepository.saveValueWithExpiry(
			REDIS_BLACKLIST_TOKEN_PREFIX + token, "blacklisted", ttl);
	}

	public boolean isBlacklisted(String token) {
		return valueRedisRepository.isValueExists(REDIS_BLACKLIST_TOKEN_PREFIX + token);
	}
}
