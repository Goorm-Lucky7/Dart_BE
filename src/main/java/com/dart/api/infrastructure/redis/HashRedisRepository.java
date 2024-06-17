package com.dart.api.infrastructure.redis;

import java.time.Duration;
import java.util.Map;

import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class HashRedisRepository {

	private final StringRedisTemplate redisTemplate;

	public void saveHashEntries(String key, Map<String, String> data) {
		HashOperations<String, Object, Object> values = redisTemplate.opsForHash();
		values.putAll(key, data);
	}

	public void saveHashEntriesWithExpiry(String key, Map<String, String> data, long duration) {
		HashOperations<String, Object, Object> values = redisTemplate.opsForHash();
		values.putAll(key, data);
		redisTemplate.expire(key, Duration.ofSeconds(duration));
	}

	@Transactional(readOnly = true)
	public String getHashEntry(String key, String hashKey) {
		HashOperations<String, Object, Object> values = redisTemplate.opsForHash();
		return Boolean.TRUE.equals(values.hasKey(key, hashKey)) ? (String)values.get(key, hashKey) : "";
	}

	public void updateHashEntry(String key, String field, String value) {
		HashOperations<String, Object, Object> values = redisTemplate.opsForHash();
		values.put(key, field, value);
	}


	public void deleteHashEntry(String key, String hashKey) {
		HashOperations<String, Object, Object> values = redisTemplate.opsForHash();
		values.delete(key, hashKey);
	}

	public void deleteAllHashEntries(String key) {
		redisTemplate.delete(key);
	}
}
