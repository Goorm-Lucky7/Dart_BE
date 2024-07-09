package com.dart.api.domain.chat.repository;

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

import com.dart.api.domain.member.entity.Member;
import com.dart.api.dto.chat.request.ChatMessageSendDto;
import com.dart.api.dto.chat.response.ChatMessageReadDto;
import com.dart.api.dto.page.PageResponse;
import com.dart.api.infrastructure.redis.ListRedisRepository;
import com.dart.support.ChatFixture;
import com.dart.support.MemberFixture;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class ChatRedisRepositoryTest {

	@Mock
	private ListRedisRepository listRedisRepository;

	@Mock
	private ObjectMapper objectMapper;

	@InjectMocks
	private ChatRedisRepository chatRedisRepository;

	@Test
	@DisplayName("SAVE CHAT MESSAGE(⭕️ SUCCESS): 성공적으로 REDIS에 채팅 메시지를 저장했습니다.")
	void saveChatMessage_void_success() throws JsonProcessingException {
		// GIVEN
		ChatMessageSendDto chatMessageSendDto = ChatFixture.createChatMessageSendDto(
			1L, 1L, "Hello 👋🏻", LocalDateTime.now(), true);
		Member member = MemberFixture.createMemberEntity();

		when(objectMapper.writeValueAsString(any(ChatMessageReadDto.class))).thenReturn("JSONValue");

		// WHEN
		chatRedisRepository.saveChatMessage(chatMessageSendDto, member);

		// THEN
		String expectedKey = REDIS_CHAT_MESSAGE_PREFIX + chatMessageSendDto.chatRoomId();
		verify(listRedisRepository).addElementWithExpiry(expectedKey, "JSONValue", chatMessageSendDto.expirySeconds());
	}

	@Test
	@DisplayName("GET CHAT MESSAGE READ DTO(⭕️ SUCCESS): 성공적으로 REDIS에 존재하는 채팅 메시지를 조회했습니다.")
	void getChatMessageReadDto_void_success() throws JsonProcessingException {
		// GIVEN
		Long chatRoomId = 1L;
		int page = 0;
		int size = 10;

		List<Object> messageValues = List.of("JSONValue1", "JSONValue2");

		when(listRedisRepository.getRange(anyString(), anyLong(), anyLong())).thenReturn(messageValues);
		when(objectMapper.readValue(anyString(), eq(ChatMessageReadDto.class)))
			.thenReturn(new ChatMessageReadDto("sender1", "content1", LocalDateTime.now(), true, "profileImageURL1"))
			.thenReturn(new ChatMessageReadDto("sender2", "content2", LocalDateTime.now(), false, "profileImageURL2"));

		// WHEN
		PageResponse<ChatMessageReadDto> pageResponse = chatRedisRepository.getChatMessageReadDto(
			chatRoomId, page, size);

		// THEN
		assertEquals(2, pageResponse.pages().size());
		assertEquals("sender1", pageResponse.pages().get(0).sender());
		assertEquals("sender2", pageResponse.pages().get(1).sender());
		assertTrue(pageResponse.pageInfo().isDone());
	}

	@Test
	@DisplayName("DELETE CHAT MESSAGES(⭕️ SUCCESS): 성공적으로 REDIS에 존재하는 모든 채팅 메시지를 삭제했습니다.")
	void deleteChatMessages_void_success() {
		// GIVEN
		Long chatRoomId = 1L;

		// WHEN
		chatRedisRepository.deleteChatMessages(chatRoomId);

		// THEN
		verify(listRedisRepository, times(1)).deleteAllElements(REDIS_CHAT_MESSAGE_PREFIX + chatRoomId);
	}
}
