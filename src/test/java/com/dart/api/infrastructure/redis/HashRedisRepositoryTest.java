package com.dart.api.infrastructure.redis;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class HashRedisRepositoryTest {

	@Mock
	private StringRedisTemplate stringRedisTemplate;

	@Mock
	private ValueOperations<String, String> valueOperations;

	@Mock
	private HashOperations<String, Object, Object> hashOperations;

	@InjectMocks
	private HashRedisRepository hashRedisRepository;

	@Test
	@DisplayName("SET EXPIRE(⭕️ SUCCESS): 성공적으로 KEY와 VALUE를 저장하고 만료 시간을 지정했습니다.")
	void setExpire_void_success() {
		// GIVEN
		String key = "testKey";
		String value = "testValue";
		long duration = 60;

		when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);

		// WHEN
		hashRedisRepository.setExpire(key, value, duration);

		// THEN
		verify(valueOperations).set(eq(key), eq(value), eq(Duration.ofSeconds(duration)));
	}

	@Test
	@DisplayName("GET VALUE(⭕️ SUCCESS): 성공적으로 KEY값으로 VALUE값을 가져왔습니다.")
	void get_value_success() {
		// GIVEN
		String expectedKey = "testKey";
		String expectedValue = "testValue";

		when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
		when(valueOperations.get(expectedKey)).thenReturn(expectedValue);

		// WHEN
		String actualValue = hashRedisRepository.get(expectedKey);

		// THEN
		assertEquals(expectedValue, actualValue);
		verify(valueOperations).get(expectedKey);
	}

	@Test
	@DisplayName("DELETE KEY(⭕️ SUCCESS): 성공적으로 KEY값을 삭제했습니다.")
	void delete_key_success() {
		// GIVEN
		String key = "testKey";

		// WHEN
		hashRedisRepository.delete(key);

		// THEN
		verify(stringRedisTemplate).delete(key);
	}

	@Test
	@DisplayName("EXISTS KEY(⭕️ SUCCESS): 성공적으로 해당 KEY값이 존재하는지 확인했습니다.")
	void exists_key_success() {
		// GIVEN
		String expectedKey = "testKey";

		when(stringRedisTemplate.hasKey(expectedKey)).thenReturn(true);

		// WHEN
		boolean actualKey = hashRedisRepository.exists(expectedKey);

		// THEN
		assertTrue(actualKey);
		verify(stringRedisTemplate).hasKey(expectedKey);
	}

	@Test
	@DisplayName("SET HASH OPS(⭕️ SUCCESS): 성공적으로 Hash KEY값으로 데이터를 저장했습니다.")
	void setHashOps_void_success() {
		// GIVEN
		String expectedKey = "testKey";

		Map<String, String> expectedData = new HashMap<>();
		expectedData.put("field1", "value1");
		expectedData.put("field2", "value2");

		when(stringRedisTemplate.opsForHash()).thenReturn(hashOperations);

		// WHEN
		hashRedisRepository.setHashOps(expectedKey, expectedData);

		// THEN
		verify(hashOperations).putAll(eq(expectedKey), eq(expectedData));
	}

	@Test
	@DisplayName("GET HASH OPS(⭕️ SUCCESS): 성공적으로 Hash KEY값으로 데이터를 가져왔습니다.")
	void getHashOps_void_success() {
		// GIVEN
		String expectedKey = "testKey";
		String expectedHashKey = "field1";
		String expectedValue = "value1";

		when(stringRedisTemplate.opsForHash()).thenReturn(hashOperations);
		when(hashOperations.hasKey(expectedKey, expectedHashKey)).thenReturn(true);
		when(hashOperations.get(expectedKey, expectedHashKey)).thenReturn(expectedValue);

		// WHEN
		String actualData = hashRedisRepository.getHashOps(expectedKey, expectedHashKey);

		// THEN
		assertEquals(expectedValue, actualData);
		verify(hashOperations).hasKey(expectedKey, expectedHashKey);
		verify(hashOperations).get(expectedKey, expectedHashKey);
	}

	@Test
	@DisplayName("DELETE HASH OPS(⭕️ SUCCESS): 성공적으로 Hash KEY값으로 데이터를 삭제했습니다.")
	void deleteHashOps_void_success() {
		// GIVEN
		String expectedKey = "testKey";
		String expectedHashKey = "field1";

		when(stringRedisTemplate.opsForHash()).thenReturn(hashOperations);

		// WHEN
		hashRedisRepository.deleteHashOps(expectedKey, expectedHashKey);

		// THEN
		verify(hashOperations).delete(expectedKey, expectedHashKey);
	}
}
