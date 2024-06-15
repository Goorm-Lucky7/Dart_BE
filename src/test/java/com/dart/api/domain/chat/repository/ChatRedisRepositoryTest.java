package com.dart.api.domain.chat.repository;

import static com.dart.global.common.util.RedisConstant.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dart.api.domain.chat.entity.ChatRoom;
import com.dart.api.dto.chat.response.ChatMessageReadDto;
import com.dart.api.infrastructure.redis.ZSetRedisRepository;
import com.dart.support.ChatFixture;

@ExtendWith(MockitoExtension.class)
class ChatRedisRepositoryTest {

	@Mock
	private ZSetRedisRepository zSetRedisRepository;

	@InjectMocks
	private ChatRedisRepository chatRedisRepository;

	@Test
	@DisplayName("SAVE CHAT MESSAGE(⭕️ SUCCESS): 성공적으로 채팅 메시지를 저장했습니다.")
	void saveChatMessage_void_success() {
		// GIVEN
		String sender = "testSender";
		String content = "Hello 👋🏻";
		LocalDateTime createdAt = LocalDateTime.now();
		long expirySeconds = 86400L;
		String messageValue = sender + "|" + content + "|" + createdAt.toString();

		ChatRoom chatRoom = ChatFixture.createChatRoomEntity();

		// WHEN
		chatRedisRepository.saveChatMessage(chatRoom, content, sender, createdAt, expirySeconds);

		// THEN
		ArgumentCaptor<Double> scoreCaptor = ArgumentCaptor.forClass(Double.class);
		ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<Long> expiryCaptor = ArgumentCaptor.forClass(Long.class);

		verify(zSetRedisRepository).addElementIfAbsent(
			keyCaptor.capture(),
			valueCaptor.capture(),
			scoreCaptor.capture(),
			expiryCaptor.capture()
		);

		assertEquals(REDIS_CHAT_MESSAGE_PREFIX + chatRoom.getId(), keyCaptor.getValue());
		assertEquals(messageValue, valueCaptor.getValue());
		assertEquals(createdAt.toEpochSecond(ZoneOffset.UTC), scoreCaptor.getValue(), 0.1);
		assertEquals(expirySeconds, expiryCaptor.getValue());
	}

	@Test
	@DisplayName("GET CHAT MESSAGE READ DTO(⭕️ SUCCESS): 성공적으로 채팅 메시지를 조회했습니다.")
	void getChatMessageReadDto_void_success() {
		// GIVEN
		Long chatRoomId = 1L;
		Set<Object> messageValues = Set.of(
			"testSender1|Hello 👋🏻|2023-01-01T12:00:00",
			"testSender2|Bye 👋🏻|2023-01-01T12:01:00"
		);

		when(zSetRedisRepository.getRange(REDIS_CHAT_MESSAGE_PREFIX + chatRoomId, 0, -1)).thenReturn(messageValues);

		// WHEN
		List<ChatMessageReadDto> actualMessages = chatRedisRepository.getChatMessageReadDto(chatRoomId);

		// THEN
		assertEquals(2, actualMessages.size());
		assertEquals("testSender1", actualMessages.get(0).sender());
		assertEquals("Hello 👋🏻", actualMessages.get(0).content());
		assertEquals(LocalDateTime.parse("2023-01-01T12:00:00"), actualMessages.get(0).createdAt());
	}

	@Test
	@DisplayName("GET CHAT MESSAGE READ DTO(❌ FAILURE): 조회된 채팅 메시지가 없을 때 빈 리스트를 반환합니다.")
	void getChatMessageReadDto_empty_fail() {
		// GIVEN
		Long chatRoomId = 1L;
		Set<Object> emptyMessageValues = Set.of();

		when(zSetRedisRepository.getRange(REDIS_CHAT_MESSAGE_PREFIX + chatRoomId, 0, -1)).thenReturn(
			emptyMessageValues);

		// WHEN
		List<ChatMessageReadDto> actualMessages = chatRedisRepository.getChatMessageReadDto(chatRoomId);

		// THEN
		assertTrue(actualMessages.isEmpty());
	}

	@Test
	@DisplayName("DELETE CHAT MESSAGES(⭕️ SUCCESS): 성공적으로 모든 채팅 메시지를 삭제했습니다.")
	void deleteChatMessages_void_success() {
		// GIVEN
		Long chatRoomId = 1L;

		// WHEN
		chatRedisRepository.deleteChatMessages(chatRoomId);

		// THEN
		verify(zSetRedisRepository).deleteAllElements(REDIS_CHAT_MESSAGE_PREFIX + chatRoomId);
	}

	@Test
	@DisplayName("DELETE CHAT MESSAGE(⭕️ SUCCESS): 성공적으로 단일 채팅 메시지를 삭제했습니다.")
	void deleteChatMessage_void_success() {
		// GIVEN
		Long chatRoomId = 1L;
		String sender = "testSender";
		String content = "Hello 👋🏻";
		LocalDateTime createdAt = LocalDateTime.now();
		String messageValue = sender + "|" + content + "|" + createdAt.toString();

		// WHEN
		chatRedisRepository.deleteChatMessage(chatRoomId, content, sender, createdAt);

		// THEN
		verify(zSetRedisRepository).removeElement(REDIS_CHAT_MESSAGE_PREFIX + chatRoomId, messageValue);
	}
}
