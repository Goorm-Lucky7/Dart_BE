package com.dart.api.domain.chat.repository;

import static com.dart.global.common.util.ChatConstant.*;
import static com.dart.global.common.util.RedisConstant.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dart.api.domain.chat.entity.ChatRoom;
import com.dart.api.domain.gallery.entity.Gallery;
import com.dart.api.domain.member.entity.Member;
import com.dart.api.dto.chat.response.ChatMessageReadDto;
import com.dart.api.dto.page.PageResponse;
import com.dart.api.infrastructure.redis.ListRedisRepository;
import com.dart.support.ChatFixture;
import com.dart.support.GalleryFixture;
import com.dart.support.MemberFixture;

@ExtendWith(MockitoExtension.class)
class ChatRedisRepositoryTest {

	@Mock
	private ListRedisRepository listRedisRepository;

	@InjectMocks
	private ChatRedisRepository chatRedisRepository;

	@Test
	@DisplayName("SAVE CHAT MESSAGE(⭕️ SUCCESS): 성공적으로 채팅 메시지를 저장했습니다.")
	void saveChatMessage_void_success() {
		// GIVEN
		String sender = "testSender";
		String content = "Hello 👋🏻";
		LocalDateTime createdAt = LocalDateTime.now();
		boolean isAuthor = false;

		String messageValue = ChatFixture.createMessageValue(sender, content, createdAt, isAuthor);

		Member member = MemberFixture.createMemberEntity();
		Gallery gallery = GalleryFixture.createGalleryEntity(member);
		ChatRoom chatRoom = ChatFixture.createChatRoomEntity(gallery);

		// WHEN
		chatRedisRepository.saveChatMessage(chatRoom, content, sender, createdAt, CHAT_MESSAGE_EXPIRY_SECONDS);

		// THEN
		verify(listRedisRepository, times(1)).addElementWithExpiry(
			eq(REDIS_CHAT_MESSAGE_PREFIX + chatRoom.getId()),
			eq(messageValue),
			eq(CHAT_MESSAGE_EXPIRY_SECONDS)
		);
	}

	@Test
	@DisplayName("GET CHAT MESSAGE READ DTO(⭕️ SUCCESS): 성공적으로 Redis에서 채팅 메시지를 조회했습니다.")
	void getChatMessageReadDto_void_success() {
		// GIVEN
		Long chatRoomId = 1L;
		int page = 0;
		int size = 10;

		long start = (long)page * size;
		long end = start + size - 1;

		List<Object> messageValues = List.of(
			ChatFixture.createMessageValue("testSender1", "Hello 👋🏻", LocalDateTime.now(), false),
			ChatFixture.createMessageValue("testSender2", "Bye 👋🏻", LocalDateTime.now(), false)
		);

		when(listRedisRepository.getRange(REDIS_CHAT_MESSAGE_PREFIX + chatRoomId, start, end))
			.thenReturn(messageValues);

		// WHEN
		PageResponse<ChatMessageReadDto> actualChatMessagePageReadDtoList =
			chatRedisRepository.getChatMessageReadDto(chatRoomId, page, size);

		// THEN
		assertEquals(2, actualChatMessagePageReadDtoList.pages().size());

		assertEquals("testSender1", actualChatMessagePageReadDtoList.pages().get(0).sender());
		assertEquals("Hello 👋🏻", actualChatMessagePageReadDtoList.pages().get(0).content());

		assertEquals("testSender2", actualChatMessagePageReadDtoList.pages().get(1).sender());
		assertEquals("Bye 👋🏻", actualChatMessagePageReadDtoList.pages().get(1).content());
	}

	@Test
	@DisplayName("DELETE CHAT MESSAGES(⭕️ SUCCESS): 성공적으로 Redis에서 모든 채팅 메시지를 삭제했습니다.")
	void deleteChatMessages_void_success() {
		// GIVEN
		Long chatRoomId = 1L;

		// WHEN
		chatRedisRepository.deleteChatMessages(chatRoomId);

		// THEN
		verify(listRedisRepository, times(1)).deleteAllElements(REDIS_CHAT_MESSAGE_PREFIX + chatRoomId);
	}
}
