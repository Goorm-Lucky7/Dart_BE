package com.dart.api.infrastructure.redis;

import static com.dart.global.common.util.ChatConstant.*;
import static com.dart.global.common.util.RedisConstant.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;

@ExtendWith(MockitoExtension.class)
class ListRedisRepositoryTest {

	@Mock
	private RedisTemplate<String, Object> redisTemplate;

	@Mock
	private ListOperations<String, Object> listOperations;

	@Mock
	private RedisConnection redisConnection;

	@Mock
	private RedisConnectionFactory redisConnectionFactory;

	@Mock
	private Cursor<byte[]> cursor;

	@InjectMocks
	private ListRedisRepository listRedisRepository;

	@Test
	@DisplayName("ADD ELEMENT(⭕️ SUCCESS): 성공적으로 List에 요소를 추가했습니다.")
	void addElement_void_success() {
		// GIVEN
		String expectedKey = "testKey";
		String expectedValue = "testValue";

		when(redisTemplate.opsForList()).thenReturn(listOperations);

		// WHEN
		listRedisRepository.addElement(expectedKey, expectedValue);

		// THEN
		verify(listOperations, times(1)).rightPush(eq(expectedKey), eq(expectedValue));
	}

	@Test
	@DisplayName("ADD ELEMENT WITH EXPIRY(⭕️ SUCCESS): 성공적으로 List에 요소를 추가하고 만료시간을 설정했습니다.")
	void addElementWithExpiry_void_success() {
		// GIVEN
		String expectedKey = "testKey";
		String expectedValue = "testValue";

		when(redisTemplate.opsForList()).thenReturn(listOperations);

		// WHEN
		listRedisRepository.addElementWithExpiry(expectedKey, expectedValue, CHAT_MESSAGE_EXPIRY_SECONDS);

		// THEN
		verify(listOperations, times(1)).rightPush(eq(expectedKey), eq(expectedValue));
		verify(redisTemplate, times(1)).expire(eq(expectedKey), eq(Duration.ofSeconds(CHAT_MESSAGE_EXPIRY_SECONDS)));
	}

	@Test
	@DisplayName("GET RANGE(⭕️ SUCCESS): 성공적으로 List의 특정 범위 요소를 조회했습니다.")
	void getRange_void_success() {
		// GIVEN
		String expectedKey = "testKey";
		long expectedStart = 0;
		long expectedEnd = 1;

		List<Object> expectedValues = Arrays.asList("testValue1", "testValue2");

		when(redisTemplate.opsForList()).thenReturn(listOperations);
		when(listOperations.range(eq(expectedKey), eq(expectedStart), eq(expectedEnd))).thenReturn(expectedValues);

		// WHEN
		List<Object> actualValues = listRedisRepository.getRange(expectedKey, expectedStart, expectedEnd);

		// THEN
		assertEquals(expectedValues, actualValues);
	}

	@Test
	@DisplayName("GET ACTIVE CHAT ROOM IDS(⭕️ SUCCESS): 성공적으로 활성 채팅방 ID 목록을 조회했습니다.")
	void getActiveChatRoomIds_void_success() {
		// GIVEN
		String chatRoomId = "123";
		String key = REDIS_CHAT_MESSAGE_PREFIX + chatRoomId;

		List<Long> expectedChatRoomIds = List.of(Long.parseLong(chatRoomId));

		when(redisTemplate.getConnectionFactory()).thenReturn(redisConnectionFactory);
		when(redisConnectionFactory.getConnection()).thenReturn(redisConnection);
		when(redisConnection.scan(any(ScanOptions.class))).thenReturn(cursor);
		when(cursor.hasNext()).thenReturn(true, false);
		when(cursor.next()).thenReturn(key.getBytes());

		// WHEN
		List<Long> actualChatRoomIds = listRedisRepository.getActiveChatRoomIds(REDIS_CHAT_MESSAGE_PREFIX);

		// THEN
		assertEquals(expectedChatRoomIds, actualChatRoomIds);
		verify(cursor, times(1)).close();
	}

	@Test
	@DisplayName("REMOVE ELEMENT(⭕️ SUCCESS): 성공적으로 List에서 요소를 제거했습니다.")
	void removeElement_void_success() {
		// GIVEN
		String expectedKey = "testKey";
		String expectedValue = "testValue";

		when(redisTemplate.opsForList()).thenReturn(listOperations);

		// WHEN
		listRedisRepository.removeElement(expectedKey, expectedValue);

		// THEN
		verify(listOperations, times(1)).remove(eq(expectedKey), eq(1L), eq(expectedValue));
	}

	@Test
	@DisplayName("REMOVE ALL ELEMENTS(⭕️ SUCCESS): 성공적으로 List에서 모든 요소를 제거했습니다.")
	void removeAllElements_void_success() {
		// GIVEN
		String expectedKey = "testKey";
		String expectedValue = "testValue";

		when(redisTemplate.opsForList()).thenReturn(listOperations);

		// WHEN
		listRedisRepository.removeAllElements(expectedKey, expectedValue);

		// THEN
		verify(listOperations, times(1)).remove(eq(expectedKey), eq(0L), eq(expectedValue));
	}

	@Test
	@DisplayName("DELETE ALL ELEMENTS(⭕️ SUCCESS): 성공적으로 List에서 모든 요소를 삭제했습니다.")
	void deleteAllElements_void_success() {
		// GIVEN
		String expectedKey = "testKey";

		// WHEN
		listRedisRepository.deleteAllElements(expectedKey);

		// THEN
		verify(redisTemplate, times(1)).delete(eq(expectedKey));
	}
}
