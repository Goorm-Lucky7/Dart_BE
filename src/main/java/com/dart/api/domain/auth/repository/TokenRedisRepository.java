package com.dart.api.domain.auth.repository;

import static com.dart.global.common.util.AuthConstant.*;
import static com.dart.global.common.util.RedisConstant.*;

import org.springframework.stereotype.Repository;

import com.dart.api.infrastructure.redis.ValueRedisRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class TokenRedisRepository {

	private final ValueRedisRepository valueRedisRepository;

	public void saveAccessToken(String key, String data) {
		valueRedisRepository.saveValueWithExpiry(REDIS_ACCESS_TOKEN_PREFIX + key, data, ACCESS_TOKEN_EXPIRATION_TIME_SECONDS);
	}

	public void saveBlacklistToken(String email, String token) {
		valueRedisRepository.saveValue(REDIS_BLACKLIST_TOKEN_PREFIX + email, token);
	}

	public void saveRefreshToken(String key, String data) {
		valueRedisRepository.saveValueWithExpiry(REDIS_REFRESH_TOKEN_PREFIX + key, data, REFRESH_TOKEN_EXPIRATION_TIME_SECONDS);
	}

	public String getAccessToken(String email) {
		return valueRedisRepository.getValue(REDIS_ACCESS_TOKEN_PREFIX + email);
	}

	public String getBlacklistToken(String email) {
		return valueRedisRepository.getValue(REDIS_BLACKLIST_TOKEN_PREFIX + email);
	}

	public String getRefreshToken(String email) {
		return valueRedisRepository.getValue(REDIS_REFRESH_TOKEN_PREFIX + email);
	}

	public void deleteAccessToken(String email) {
		valueRedisRepository.deleteValue(REDIS_ACCESS_TOKEN_PREFIX + email);
	}

	public void deleteBlacklistToken(String email) {
		valueRedisRepository.deleteValue(REDIS_BLACKLIST_TOKEN_PREFIX + email);
	}

	public void deleteRefreshToken(String email) {
		valueRedisRepository.deleteValue(REDIS_REFRESH_TOKEN_PREFIX + email);
	}

	public boolean checkAccessTokenExists(String email) {
		return valueRedisRepository.isValueExists(REDIS_ACCESS_TOKEN_PREFIX + email);
	}

	public boolean checkRefreshTokenExists(String email) {
		return valueRedisRepository.isValueExists(REDIS_REFRESH_TOKEN_PREFIX + email);
	}
}
