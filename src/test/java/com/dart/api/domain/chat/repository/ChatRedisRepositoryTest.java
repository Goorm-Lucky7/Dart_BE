package com.dart.api.domain.chat.repository;

import static com.dart.global.common.util.RedisConstant.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
		long expirySeconds = 3600;
		String messageValue = sender + "|" + content + "|" + createdAt.toString();

		ChatRoom chatRoom = ChatFixture.createChatRoomEntity();

		// WHEN
		chatRedisRepository.saveChatMessage(chatRoom, content, sender, createdAt, expirySeconds);

		// THEN
		verify(zSetRedisRepository).addElementIfAbsent(
			eq(REDIS_CHAT_MESSAGE_PREFIX + chatRoom.getId()),
			eq(messageValue),
			doubleThat(value -> value == createdAt.toEpochSecond(ZoneOffset.UTC)),
			eq(expirySeconds)
		);
	}

	@Test
	@DisplayName("SAVE CHAT MESSAGE(⭕️ SUCCESS): 만료 시간이 없는 채팅 메시지를 성공적으로 저장했습니다.")
	void saveChatMessage_expiry_success() {
		// GIVEN
		String sender = "testSender";
		String content = "Hello 👋🏻";
		LocalDateTime createdAt = LocalDateTime.now();
		long expirySeconds = -1;
		String messageValue = sender + "|" + content + "|" + createdAt.toString();

		ChatRoom chatRoom = ChatFixture.createChatRoomEntity();

		// WHEN
		chatRedisRepository.saveChatMessage(chatRoom, content, sender, createdAt, expirySeconds);

		// THEN
		verify(zSetRedisRepository).addElementIfAbsent(
			eq(REDIS_CHAT_MESSAGE_PREFIX + chatRoom.getId()),
			eq(messageValue),
			eq((double)createdAt.toEpochSecond(ZoneOffset.UTC)),
			eq(expirySeconds)
		);

		verify(zSetRedisRepository, never()).addElement(
			eq(REDIS_CHAT_MESSAGE_PREFIX + chatRoom.getId()),
			eq(messageValue),
			anyDouble()
		);
	}

	@Test
	@DisplayName("GET CHAT MESSAGE READ DTO(⭕️ SUCCESS): 성공적으로 채팅 메시지를 조회했습니다.")
	void getChatMessageReadDto_void_success() {
		// GIVEN
		Long chatRoomId = 1L;
		String sender = "testSender1";
		String content = "Hello 👋🏻";
		LocalDateTime createdAt = LocalDateTime.now();
		String messageValue = sender + "|" + content + "|" + createdAt.toString();

		Set<Object> messageValues = Set.of(messageValue);

		when(zSetRedisRepository.getRange(eq(REDIS_CHAT_MESSAGE_PREFIX + chatRoomId), eq(0L), eq(-1L)))
			.thenReturn(messageValues);

		// WHEN
		List<ChatMessageReadDto> actualMessages = chatRedisRepository.getChatMessageReadDto(chatRoomId);

		// THEN
		assertThat(actualMessages).isNotEmpty();
		assertThat(actualMessages).hasSize(1);
		assertThat(actualMessages.get(0).sender()).isEqualTo(sender);
		assertThat(actualMessages.get(0).content()).isEqualTo(content);
		assertThat(actualMessages.get(0).createdAt()).isEqualTo(createdAt);
	}

	@Test
	@DisplayName("GET CHAT MESSAGE READ DTO(❌ FAILURE): 조회된 채팅 메시지가 없을 때 빈 리스트를 반환합니다.")
	void getChatMessageReadDto_empty_fail() {
		// GIVEN
		Long chatRoomId = 1L;
		Set<Object> emptyMessageValues = Set.of();

		when(zSetRedisRepository.getRange(REDIS_CHAT_MESSAGE_PREFIX + chatRoomId, 0, -1))
			.thenReturn(emptyMessageValues);

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
