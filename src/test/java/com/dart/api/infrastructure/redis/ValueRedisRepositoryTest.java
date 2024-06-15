package com.dart.api.infrastructure.redis;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Duration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class ValueRedisRepositoryTest {

	@Mock
	private StringRedisTemplate stringRedisTemplate;

	@Mock
	private ValueOperations<String, String> valueOperations;

	@InjectMocks
	private ValueRedisRepository valueRedisRepository;

	@Test
	@DisplayName("SAVE VALUE WITH EXPIRY(⭕️ SUCCESS): 성공적으로 KEY와 VALUE를 저장하고 만료 시간을 지정했습니다.")
	void saveValueWithExpiry_void_success() {
		// GIVEN
		String key = "testKey";
		String value = "testValue";
		long duration = 60;

		when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);

		// WHEN
		valueRedisRepository.saveValueWithExpiry(key, value, duration);

		// THEN
		verify(valueOperations).set(eq(key), eq(value), eq(Duration.ofSeconds(duration)));
	}

	@Test
	@DisplayName("GET VALUE(⭕️ SUCCESS): 성공적으로 KEY값으로 VALUE값을 가져왔습니다.")
	void getValue_void_success() {
		// GIVEN
		String expectedKey = "testKey";
		String expectedValue = "testValue";

		when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
		when(valueOperations.get(expectedKey)).thenReturn(expectedValue);

		// WHEN
		String actualValue = valueRedisRepository.getValue(expectedKey);

		// THEN
		assertEquals(expectedValue, actualValue);
		verify(valueOperations).get(expectedKey);
	}

	@Test
	@DisplayName("DELETE VALUE(⭕️ SUCCESS): 성공적으로 KEY값을 삭제했습니다.")
	void deleteValue_void_success() {
		// GIVEN
		String key = "testKey";

		// WHEN
		valueRedisRepository.deleteValue(key);

		// THEN
		verify(stringRedisTemplate).delete(key);
	}

	@Test
	@DisplayName("IS VALUE EXISTS(⭕️ SUCCESS): 성공적으로 해당 KEY값이 존재하는지 확인했습니다.")
	void isValueExists_void_success() {
		// GIVEN
		String expectedKey = "testKey";

		when(stringRedisTemplate.hasKey(expectedKey)).thenReturn(true);

		// WHEN
		boolean actualKey = valueRedisRepository.isValueExists(expectedKey);

		// THEN
		assertTrue(actualKey);
		verify(stringRedisTemplate).hasKey(expectedKey);
	}
}
