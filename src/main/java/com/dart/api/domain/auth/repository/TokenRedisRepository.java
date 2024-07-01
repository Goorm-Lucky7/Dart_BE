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

	@Value("${jwt.access-expire}")
	private long accessTokenExpire;

	@Value("${jwt.refresh-expire}")
	private long refreshTokenExpire;

	private final ValueRedisRepository valueRedisRepository;

	public void setAccessToken(String key, String data) {
		valueRedisRepository.saveValueWithExpiry(REDIS_ACCESS_TOKEN_PREFIX + key, data, accessTokenExpire/1000);
	}
	public void setRefreshToken(String key, String data) {
		valueRedisRepository.saveValueWithExpiry(REDIS_REFRESH_TOKEN_PREFIX + key, data, refreshTokenExpire/1000);
	}

	@Transactional(readOnly = true)
	public String getAccessToken(String key) {
		String value = valueRedisRepository.getValue(REDIS_ACCESS_TOKEN_PREFIX + key);
		return value != null ? value : "false";
	}

	@Transactional(readOnly = true)
	public String getRefreshToken(String key) {
		String value = valueRedisRepository.getValue(REDIS_REFRESH_TOKEN_PREFIX + key);
		return value != null ? value : "false";
	}

	public void deleteAccessToken(String key) {
		valueRedisRepository.deleteValue(REDIS_ACCESS_TOKEN_PREFIX + key);
	}

	public void deleteRefreshToken(String key) {
		valueRedisRepository.deleteValue(REDIS_REFRESH_TOKEN_PREFIX + key);
	}
}
