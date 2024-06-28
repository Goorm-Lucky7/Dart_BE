package com.dart.api.infrastructure.redis;

import static java.util.Objects.*;

import java.time.Duration;
import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ZSetRedisRepository {

	private final StringRedisTemplate redisTemplate;

	public void addElement(String key, String value, double score) {
		redisTemplate.opsForZSet().add(requireNonNull(key), requireNonNull(value), score);
	}

	public boolean addElementIfAbsent(String key, String value, double score) {
		return Boolean.TRUE.equals(
			redisTemplate.opsForZSet().addIfAbsent(requireNonNull(key), requireNonNull(value), score)
		);
	}

	public void addElementWithExpiry(String key, String value, double score, long expire) {
		redisTemplate.opsForZSet().add(requireNonNull(key), requireNonNull(value), score);

		if (expire > 0) {
			redisTemplate.expire(key, Duration.ofSeconds(expire));
		}
	}

	public Set<String> getRange(String key, long start, long end) {
		return redisTemplate.opsForZSet().range(requireNonNull(key), start, end);
	}

	public void removeElement(String key, String value) {
		redisTemplate.opsForZSet().remove(requireNonNull(key), requireNonNull(value));
	}

	public void deleteAllElements(String key) {
		redisTemplate.delete(requireNonNull(key));
	}

	public Double score(String key, String value) {
		return redisTemplate
			.opsForZSet()
			.score(key, value);
	}

	public Long size(String key) {
		return redisTemplate
			.opsForZSet()
			.zCard(key);
	}
}
