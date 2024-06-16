package com.dart.api.infrastructure.redis;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

@ExtendWith(MockitoExtension.class)
class HashRedisRepositoryTest {

	@Mock
	private StringRedisTemplate stringRedisTemplate;

	@Mock
	private HashOperations<String, Object, Object> hashOperations;

	@InjectMocks
	private HashRedisRepository hashRedisRepository;

	@Test
	@DisplayName("SAVE HASH ENTRIES(⭕️ SUCCESS): 성공적으로 Hash KEY값으로 데이터를 저장했습니다.")
	void saveHashEntries_void_success() {
		// GIVEN
		String expectedKey = "testKey";

		Map<String, String> expectedData = new HashMap<>();
		expectedData.put("field1", "value1");
		expectedData.put("field2", "value2");

		when(stringRedisTemplate.opsForHash()).thenReturn(hashOperations);

		// WHEN
		hashRedisRepository.saveHashEntries(expectedKey, expectedData);

		// THEN
		verify(hashOperations).putAll(eq(expectedKey), eq(expectedData));
	}

	@Test
	@DisplayName("GET HASH ENTRY(⭕️ SUCCESS): 성공적으로 Hash KEY값으로 데이터를 가져왔습니다.")
	void getHashEntry_void_success() {
		// GIVEN
		String expectedKey = "testKey";
		String expectedHashKey = "field1";
		String expectedValue = "value1";

		when(stringRedisTemplate.opsForHash()).thenReturn(hashOperations);
		when(hashOperations.hasKey(expectedKey, expectedHashKey)).thenReturn(true);
		when(hashOperations.get(expectedKey, expectedHashKey)).thenReturn(expectedValue);

		// WHEN
		String actualData = hashRedisRepository.getHashEntry(expectedKey, expectedHashKey);

		// THEN
		assertEquals(expectedValue, actualData);
		verify(hashOperations).hasKey(expectedKey, expectedHashKey);
		verify(hashOperations).get(expectedKey, expectedHashKey);
	}

	@Test
	@DisplayName("DELETE HASH ENTRY(⭕️ SUCCESS): 성공적으로 Hash KEY값으로 데이터를 삭제했습니다.")
	void deleteHashEntry_void_success() {
		// GIVEN
		String expectedKey = "testKey";
		String expectedHashKey = "field1";

		when(stringRedisTemplate.opsForHash()).thenReturn(hashOperations);

		// WHEN
		hashRedisRepository.deleteHashEntry(expectedKey, expectedHashKey);

		// THEN
		verify(hashOperations).delete(expectedKey, expectedHashKey);
	}
}
