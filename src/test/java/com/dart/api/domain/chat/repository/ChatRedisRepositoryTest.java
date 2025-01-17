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
import com.dart.api.dto.chat.request.ChatMessageCreateDto;
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
	@DisplayName("SAVE BATCH CHAT MESSAGE(⭕️ SUCCESS): 성공적으로 BATCH REDIS에 채팅 메시지를 저장했습니다.")
	void saveBatchChatMessage_void_success() throws JsonProcessingException {
		// GIVEN
		ChatMessageSendDto chatMessageSendDto = ChatFixture.createChatMessageSendDto(
			1L, 1L, "Hello 👋🏻", LocalDateTime.now(), true);
		Member member = MemberFixture.createMemberEntity();

		when(objectMapper.writeValueAsString(any(ChatMessageReadDto.class))).thenReturn("JSONValue");

		// WHEN
		chatRedisRepository.saveBatchChatMessage(chatMessageSendDto, member);

		// THEN
		String expectedKey = REDIS_BATCH_CHAT_MESSAGE_PREFIX + chatMessageSendDto.chatRoomId();
		verify(listRedisRepository).addElement(expectedKey, "JSONValue");
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
	@DisplayName("GET ALL BATCH CHAT MESSAGE(⭕️ SUCCESS): 성공적으로 BATCH 채팅 메시지를 조회했습니다.")
	void getAllBatchMessages_void_success() throws JsonProcessingException {
		// GIVEN
		List<Object> batchMessageValues = List.of("JSONValue1", "JSONValue2");

		Long chatRoomId = 1L;

		ChatMessageCreateDto chatMessageCreateDto1 = ChatFixture.createChatMessageEntityForChatMessageCreateDto();
		ChatMessageCreateDto chatMessageCreateDto2 = ChatFixture.createChatMessageEntityForChatMessageCreateDto();

		when(listRedisRepository.getRange(
			eq(REDIS_BATCH_CHAT_MESSAGE_PREFIX + chatRoomId.toString()),
			eq((long)REDIS_BATCH_START_INDEX),
			eq((long)(REDIS_BATCH_END_INDEX - 1)))).thenReturn(batchMessageValues);
		when(objectMapper.readValue(eq("JSONValue1"), eq(ChatMessageCreateDto.class))).thenReturn(
			chatMessageCreateDto1);
		when(objectMapper.readValue(eq("JSONValue2"), eq(ChatMessageCreateDto.class))).thenReturn(
			chatMessageCreateDto2);

		// WHEN
		List<ChatMessageCreateDto> batchChatMessageList = chatRedisRepository.getAllBatchMessages(chatRoomId);

		// THEN
		assertEquals(2, batchChatMessageList.size());
		assertEquals(chatMessageCreateDto1, batchChatMessageList.get(0));
		assertEquals(chatMessageCreateDto2, batchChatMessageList.get(1));
	}

	@Test
	@DisplayName("GET ACTIVE CHAT ROOM IDS(⭕️ SUCCESS): 성공적으로 활성 채팅방 ID 목록을 조회했습니다.")
	void getActiveChatRoomIds_void_success() {
		// GIVEN
		Long chatRoomId = 1L;

		List<Long> expectedChatRoomIds = List.of(chatRoomId);

		when(listRedisRepository.getActiveChatRoomIds(REDIS_CHAT_MESSAGE_PREFIX)).thenReturn(expectedChatRoomIds);

		// WHEN
		List<Long> actualChatRoomIds = chatRedisRepository.getActiveChatRoomIds();

		// THEN
		assertEquals(expectedChatRoomIds, actualChatRoomIds);
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

	@Test
	@DisplayName("DELETE BATCH CHAT MESSAGES(⭕️ SUCCESS): 성공적으로 BATCH REDIS에 존재하는 모든 채팅 메시지를 삭제했습니다.")
	void deleteBatchChatMessages_void_success() {
		// GIVEN
		Long chatRoomId = 1L;

		// WHEN
		chatRedisRepository.deleteBatchChatMessages(chatRoomId);

		// THEN
		verify(listRedisRepository, times(1)).deleteAllElements(REDIS_BATCH_CHAT_MESSAGE_PREFIX + chatRoomId);
	}
}
