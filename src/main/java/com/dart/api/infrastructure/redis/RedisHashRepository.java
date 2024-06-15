package com.dart.api.infrastructure.redis;

import java.time.Duration;
import java.util.Map;

import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
@RequiredArgsConstructor
public class RedisHashRepository {

	private final StringRedisTemplate redisTemplate;

	public String get(String key) {
		ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
		return valueOperations.get(key);
	}

	public boolean exists(String key) {
		return Boolean.TRUE.equals(redisTemplate.hasKey(key));
	}

	public void setExpire(String key, String data, long duration) {
		ValueOperations<String, String> value = redisTemplate.opsForValue();
		Duration expireDuration = Duration.ofSeconds(duration);
		value.set(key, data, expireDuration);
	}

	public void delete(String key) {
		redisTemplate.delete(key);
	}

	public void setHashOpsAndExpire(String key, Map<String, String> data, long duration) {
		HashOperations<String, Object, Object> values = redisTemplate.opsForHash();
		values.putAll(key, data);
		redisTemplate.expire(key, Duration.ofSeconds(duration));
	}

	@Transactional(readOnly = true)
	public String getHashOps(String key, String hashKey) {
		HashOperations<String, Object, Object> values = redisTemplate.opsForHash();
		return (String) values.get(key, hashKey);
	}

	public void updateHashOpsField(String key, String field, String value) {
		HashOperations<String, Object, Object> values = redisTemplate.opsForHash();
		values.put(key, field, value);
	}

	public void deleteHashOps(String key, String hashKey) {
		HashOperations<String, Object, Object> values = redisTemplate.opsForHash();
		values.delete(key, hashKey);
	}
}
