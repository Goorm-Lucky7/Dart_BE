package com.dart.api.infrastructure.redis;

import static com.dart.api.infrastructure.redis.RedisConstant.REDIS_TOKEN_PREFIX;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class RedisTokenRepository {

	@Value("${jwt.refresh-expire}")
	private long refreshTokenExpire;

	private final RedisHashRepository redisHashRepository;

	public void setToken(String key, String data) {
		redisHashRepository.setExpire(REDIS_TOKEN_PREFIX + key, data, refreshTokenExpire/1000);
	}

	@Transactional(readOnly = true)
	public String getToken(String key) {
		String value = redisHashRepository.get(REDIS_TOKEN_PREFIX + key);
		return value != null ? value : "false";
	}

	public void deleteToken(String key) {
		redisHashRepository.delete(REDIS_TOKEN_PREFIX + key);
	}
}
