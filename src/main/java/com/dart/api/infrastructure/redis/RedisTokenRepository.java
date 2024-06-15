package com.dart.api.infrastructure.redis;

import static com.dart.api.infrastructure.redis.RedisConstant.*;
import static com.dart.global.common.util.AuthConstant.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class RedisTokenRepository {

	private final RedisHashRepository redisHashRepository;

	public void setToken(String key, String data) {
		redisHashRepository.setExpire(TOKEN_PREFIX + key, data, REFRESH_TOKEN_EXPIRATION_TIME_SECONDS);
	}

	@Transactional(readOnly = true)
	public String getToken(String key) {
		String value = redisHashRepository.get(TOKEN_PREFIX + key);
		return value != null ? value : "false";
	}

	public void deleteToken(String key) {
		redisHashRepository.delete(TOKEN_PREFIX + key);
	}
}
