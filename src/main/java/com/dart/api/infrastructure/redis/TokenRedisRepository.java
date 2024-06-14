package com.dart.api.infrastructure.redis;

import static com.dart.global.common.util.RedisConstant.REDIS_TOKEN_PREFIX;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class TokenRedisRepository {

	@Value("${jwt.refresh-expire}")
	private long refreshTokenExpire;

	private final HashRedisRepository hashRedisRepository;

	public void setToken(String key, String data) {
		hashRedisRepository.setExpire(REDIS_TOKEN_PREFIX + key, data, refreshTokenExpire/1000);
	}

	@Transactional(readOnly = true)
	public String getToken(String key) {
		String value = hashRedisRepository.get(REDIS_TOKEN_PREFIX + key);
		return value != null ? value : "false";
	}

	public void deleteToken(String key) {
		hashRedisRepository.delete(REDIS_TOKEN_PREFIX + key);
	}
}
