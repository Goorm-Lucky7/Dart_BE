package com.dart.api.infrastructure.redis;

import static java.util.Objects.*;

import java.time.Duration;
import java.util.List;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ListRedisRepository {

	private final RedisTemplate<String, Object> redisTemplate;

	public void addElement(String key, Object value) {
		redisTemplate.opsForList().rightPush(requireNonNull(key), requireNonNull(value));
	}

	public void addElementWithExpiry(String key, Object value, long expire) {
		redisTemplate.opsForList().rightPush(requireNonNull(key), requireNonNull(value));
		if (expire > 0) {
			redisTemplate.expire(key, Duration.ofSeconds(expire));
		}
	}

	public List<Object> getRange(String key, long start, long end) {
		return redisTemplate.opsForList().range(requireNonNull(key), start, end);
	}

	public void removeElement(String key, Object value) {
		redisTemplate.opsForList().remove(requireNonNull(key), 1, requireNonNull(value));
	}

	public void removeAllElements(String key, Object value) {
		redisTemplate.opsForList().remove(requireNonNull(key), 0, requireNonNull(value));
	}

	public void deleteAllElements(String key) {
		redisTemplate.delete(requireNonNull(key));
	}
}
