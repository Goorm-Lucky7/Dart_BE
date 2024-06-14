package com.dart.api.infrastructure.redis;

import static java.util.Objects.*;

import java.time.Duration;
import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ZSetRedisRepository {

	private final RedisTemplate<String, Object> redisTemplate;

	public void addElement(String key, String value, double score) {
		redisTemplate.opsForZSet().add(requireNonNull(key), requireNonNull(value), score);
	}

	public void addElementIfAbsent(String key, Object value, double score, long expire) {
		redisTemplate.opsForZSet().addIfAbsent(requireNonNull(key), requireNonNull(value), score);
		redisTemplate.expire(key, Duration.ofSeconds(expire));
	}

	public Set<Object> getRange(String key, long start, long end) {
		return redisTemplate.opsForZSet().range(requireNonNull(key), start, end);
	}

	public void removeElement(String key, String value) {
		redisTemplate.opsForZSet().remove(requireNonNull(key), requireNonNull(value));
	}

	public void deleteAllElements(String key) {
		redisTemplate.delete(requireNonNull(key));
	}
}