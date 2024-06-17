package com.dart.api.presentation;

import static com.dart.global.common.util.ChatConstant.*;
import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.dart.api.application.chat.ChatService;
import com.dart.api.domain.auth.entity.AuthUser;
import com.dart.api.dto.chat.request.ChatMessageCreateDto;
import com.dart.api.dto.chat.response.ChatMessageReadDto;
import com.dart.api.infrastructure.websocket.MemberSessionRegistry;
import com.dart.support.ChatFixture;
import com.dart.support.MemberFixture;

@ExtendWith(SpringExtension.class)
class ChatControllerTest {

	@Mock
	private ChatService chatService;

	@Mock
	private SimpMessageSendingOperations simpMessageSendingOperations;

	@Mock
	private SimpMessageHeaderAccessor simpMessageHeaderAccessor;

	@Mock
	private MemberSessionRegistry memberSessionRegistry;

	@InjectMocks
	private ChatController chatController;

	private MockMvc mockMvc;

	@Test
	@DisplayName("SAVE AND SEND CHAT MESSAGE(⭕️ SUCCESS): STOMP 메시지가 성공적으로 전송되었습니다.")
	void saveAndSendChatMessage_void_success() {
		// GIVEN
		Long chatRoomId = 1L;

		AuthUser authUser = MemberFixture.createAuthUserEntity();
		ChatMessageCreateDto chatMessageCreateDto = ChatFixture.createChatMessageEntityForChatMessageCreateDto();

		Map<String, Object> sessionAttributes = new HashMap<>();
		sessionAttributes.put(CHAT_SESSION_USER, authUser);

		given(simpMessageHeaderAccessor.getSessionAttributes()).willReturn(sessionAttributes);

		// WHEN
		chatController.saveAndSendChatMessage(chatRoomId, chatMessageCreateDto, simpMessageHeaderAccessor);

		// THEN
		verify(chatService).saveChatMessage(chatRoomId, chatMessageCreateDto, simpMessageHeaderAccessor);
		verify(simpMessageSendingOperations).convertAndSend("/sub/ws/" + chatRoomId, chatMessageCreateDto.content());
	}

	@Test
	@DisplayName("GET LOGGED-IN VISITORS(⭕️ SUCCESS): 성공적으로 채팅방에 로그인된 사용자 목록을 조회했습니다.")
	void getLoggedInVisitors_void_success() {
		// GIVEN
		Long chatRoomId = 1L;
		String destination = "/sub/ws/" + chatRoomId;
		List<String> members = Arrays.asList("member1", "member2", "member3");

		given(memberSessionRegistry.getMembersInChatRoom(destination)).willReturn(members);

		// WHEN
		ResponseEntity<List<String>> responseEntity = chatController.getLoggedInVisitors(chatRoomId);

		// THEN
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(responseEntity.getBody()).containsExactly("member1", "member2", "member3");
	}

	@Test
	@DisplayName("GET LOGGED-IN VISITORS(⭕️ SUCCESS): 채팅방에 사용자가 없는 경우 빈 배열을 반환합니다.")
	void getLoggedInVisitors_empty_success() {
		// GIVEN
		Long chatRoomId = 1L;
		String destination = "/sub/ws/" + chatRoomId;

		given(memberSessionRegistry.getMembersInChatRoom(destination)).willReturn(List.of());

		// WHEN
		ResponseEntity<List<String>> responseEntity = chatController.getLoggedInVisitors(chatRoomId);

		// THEN
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(responseEntity.getBody()).isEmpty();
	}

	@Test
	@DisplayName("GET LOGGED-IN VISITORS(⭕️ SUCCESS): 성공적으로 채팅 메시지 목록을 조회했습니다.")
	void getChatMessageList_void_success() throws Exception {
		// GIVEN
		Long chatRoomId = 1L;

		List<ChatMessageReadDto> chatMessagesList = List.of(
			new ChatMessageReadDto("testSender1", "Hello 👋🏻", LocalDateTime.parse("2023-01-01T12:00:00")),
			new ChatMessageReadDto("testSender2", "Bye 👋🏻", LocalDateTime.parse("2023-01-01T12:01:00"))
		);

		given(chatService.getChatMessageList(chatRoomId)).willReturn(chatMessagesList);

		// WHEN
		mockMvc = MockMvcBuilders.standaloneSetup(chatController).build();

		// THEN
		mockMvc.perform(get("/api/{chat-room-id}/chat-messages", chatRoomId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(2)))
			.andExpect(jsonPath("$[0].sender", is("testSender1")))
			.andExpect(jsonPath("$[0].content", is("Hello 👋🏻")))
			.andExpect(jsonPath("$[0].createdAt", contains(2023, 1, 1, 12, 0)))
			.andExpect(jsonPath("$[1].sender", is("testSender2")))
			.andExpect(jsonPath("$[1].content", is("Bye 👋🏻")))
			.andExpect(jsonPath("$[1].createdAt", contains(2023, 1, 1, 12, 1)));
	}

	@Test
	@DisplayName("GET CHAT MESSAGE LIST(❌ FAILURE): 조회된 채팅 메시지가 없을 때 빈 리스트를 반환합니다.")
	void getChatMessageList_empty_fail() throws Exception {
		// GIVEN
		Long chatRoomId = 1L;

		given(chatService.getChatMessageList(chatRoomId)).willReturn(List.of());

		// WHEN
		mockMvc = MockMvcBuilders.standaloneSetup(chatController).build();

		// THEN
		mockMvc.perform(get("/api/{chat-room-id}/chat-messages", chatRoomId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(0)));
	}
}
