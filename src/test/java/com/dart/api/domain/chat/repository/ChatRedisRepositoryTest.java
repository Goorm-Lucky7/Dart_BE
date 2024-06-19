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
		long expirySeconds = 7 * 24 * 60 * 60;
		boolean isAuthor = false;

		String messageValue = ChatFixture.createMessageValue(sender, content, createdAt, isAuthor);
		ChatRoom chatRoom = ChatFixture.createChatRoomEntity();

		// WHEN
		chatRedisRepository.saveChatMessage(chatRoom, content, sender, createdAt, expirySeconds);

		// THEN
		verify(zSetRedisRepository).addElementWithExpiry(
			eq(REDIS_CHAT_MESSAGE_PREFIX + chatRoom.getId()),
			eq(messageValue),
			doubleThat(value -> value == createdAt.toEpochSecond(ZoneOffset.UTC)),
			eq(expirySeconds)
		);
	}

	@Test
	@DisplayName("GET CHAT MESSAGE READ DTO(⭕️ SUCCESS): 성공적으로 채팅 메시지를 조회했습니다.")
	void getChatMessageReadDto_void_success() {
		// GIVEN
		Long chatRoomId = 1L;
		long ZSET_START_INDEX = 0;
		long ZSET_END_INDEX_ALL = -1;

		Set<Object> messageValues = Set.of(
			ChatFixture.createMessageValue("testSender1", "Hello 👋🏻", LocalDateTime.now(), false),
			ChatFixture.createMessageValue("testSender2", "Bye 👋🏻", LocalDateTime.now(), false)
		);

		when(zSetRedisRepository.getRange(
			eq(REDIS_CHAT_MESSAGE_PREFIX + chatRoomId),
			eq(ZSET_START_INDEX),
			eq(ZSET_END_INDEX_ALL))
		).thenReturn(messageValues);

		// WHEN
		List<ChatMessageReadDto> actualMessages = chatRedisRepository.getChatMessageReadDto(chatRoomId);

		// THEN
		assertEquals(2, actualMessages.size());
		verify(zSetRedisRepository).getRange(
			eq(REDIS_CHAT_MESSAGE_PREFIX + chatRoomId),
			eq(ZSET_START_INDEX),
			eq(ZSET_END_INDEX_ALL));
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
}
