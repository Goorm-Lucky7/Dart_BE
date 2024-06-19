package com.dart.api.application.chat;

import static com.dart.global.common.util.RedisConstant.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dart.api.domain.chat.entity.ChatRoom;
import com.dart.api.domain.chat.repository.ChatMessageRepository;
import com.dart.api.domain.chat.repository.ChatRedisRepository;
import com.dart.api.domain.chat.repository.ChatRoomRepository;
import com.dart.api.dto.chat.response.ChatMessageReadDto;
import com.dart.support.ChatFixture;

@ExtendWith(MockitoExtension.class)
class ChatMessageArchiveServiceTest {

	@Mock
	private ChatMessageRepository chatMessageRepository;

	@Mock
	private ChatRedisRepository chatRedisRepository;

	@Mock
	private ChatRoomRepository chatRoomRepository;

	@InjectMocks
	private ChatMessageArchiveService chatMessageArchiveService;

	@Test
	@DisplayName("HANDLE REDIS EXPIRED EVENT(⭕️ SUCCESS): 성공적으로 Redis 만료 이벤트를 처리하고 메시지를 아카이브 했습니다.")
	void handleRedisExpiredEvent_void_success() {
		// GIVEN
		Long chatRoomId = 1L;

		ChatRoom chatRoom = ChatFixture.createChatRoomEntity();
		List<ChatMessageReadDto> expiredMessages = List.of(
			ChatFixture.createChatMessageReadDto("testSender1", "Hello 👋🏻", LocalDateTime.now(), false),
			ChatFixture.createChatMessageReadDto("testSender2", "Bye 👋🏻", LocalDateTime.now(), false)
		);

		when(chatRedisRepository.getChatMessageReadDto(chatRoomId)).thenReturn(expiredMessages);
		when(chatRoomRepository.findById(chatRoomId)).thenReturn(Optional.of(chatRoom));

		// WHEN
		chatMessageArchiveService.handleRedisExpiredEvent(REDIS_CHAT_MESSAGE_PREFIX + chatRoomId);

		// THEN
		verify(chatRedisRepository).deleteChatMessages(chatRoomId);
		verify(chatMessageRepository).saveAll(anyList());
	}
}
