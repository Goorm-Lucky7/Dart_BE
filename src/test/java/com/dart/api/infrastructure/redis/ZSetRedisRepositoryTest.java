package com.dart.api.infrastructure.redis;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

@ExtendWith(MockitoExtension.class)
class ZSetRedisRepositoryTest {

	@Mock
	private RedisTemplate<String, Object> redisTemplate;

	@Mock
	ZSetOperations<String, Object> zSetOperations;

	@InjectMocks
	private ZSetRedisRepository zSetRedisRepository;

	@Test
	@DisplayName("ADD ELEMENT(⭕️ SUCCESS): 성공적으로 ZSet에 요소를 추가했습니다.")
	void addElement() {
		// GIVEN
		String expectedKey = "testKey";
		String expectedValue = "testValue";
		double expectedScore = 1.0;

		when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);

		// WHEN
		zSetRedisRepository.addElement(expectedKey, expectedValue, expectedScore);

		// THEN
		verify(zSetOperations).add(eq(expectedKey), eq(expectedValue), eq(expectedScore));
	}

	@Test
	@DisplayName("ADD ELEMENT IF ABSENT(⭕️ SUCCESS): 요청 조건을 만족하여 성공적으로 ZSet에 요소를 추가했습니다.")
	void addElementIfAbsent_void_success() {
		// GIVEN
		String expectedKey = "testKey";
		String expectedValue = "testValue";
		double expectedScore = 1.0;
		long expectedExpiry = 600;

		when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);

		// WHEN
		zSetRedisRepository.addElementIfAbsent(expectedKey, expectedValue, expectedScore, expectedExpiry);

		// THEN
		verify(zSetOperations).addIfAbsent(eq(expectedKey), eq(expectedValue), eq(expectedScore));
		verify(redisTemplate).expire(eq(expectedKey), eq(Duration.ofSeconds(expectedExpiry)));
	}

	@Test
	@DisplayName("GET RANGE(⭕️ SUCCESS): 성공적으로 ZSet에서 범위 내의 요소를 조회했습니다.")
	void getRange_void_success() {
		// GIVEN
		String expectedKey = "testKey";
		long expectedStart = 0;
		long expectedEnd = -1;

		Set<Object> expectedValues = Set.of("element1", "element2");

		when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
		when(zSetOperations.range(expectedKey, expectedStart, expectedEnd)).thenReturn(expectedValues);

		// WHEN
		Set<Object> actualValues = zSetRedisRepository.getRange(expectedKey, expectedStart, expectedEnd);

		// THEN
		assertEquals(expectedValues, actualValues);
		verify(zSetOperations).range(eq(expectedKey), eq(expectedStart), eq(expectedEnd));
	}

	@Test
	@DisplayName("REMOVE ELEMENT(⭕️ SUCCESS): 성공적으로 ZSet에서 요소를 삭제했습니다.")
	void removeElement_void_success() {
		// GIVEN
		String expectedKey = "testKey";
		String expectedValue = "testValue";

		when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);

		// WHEN
		zSetRedisRepository.removeElement(expectedKey, expectedValue);

		// THEN
		verify(zSetOperations).remove(eq(expectedKey), eq(expectedValue));
	}

	@Test
	@DisplayName("DELETE ALL ELEMENTS(⭕️ SUCCESS): 성공적으로 ZSet에서 모든 요소들을 삭제했습니다.")
	void deleteAllElements_void_success() {
		// GIVEN
		String expectedKey = "testKey";

		// WHEN
		zSetRedisRepository.deleteAllElements(expectedKey);

		// THEN
		verify(redisTemplate).delete(eq(expectedKey));
	}
}
