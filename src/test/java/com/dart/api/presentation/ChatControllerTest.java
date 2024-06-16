package com.dart.api.presentation;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

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

import com.dart.api.application.chat.ChatService;
import com.dart.api.domain.auth.entity.AuthUser;
import com.dart.api.dto.chat.request.ChatMessageCreateDto;
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

	@Test
	@DisplayName("SAVE AND SEND CHAT MESSAGE(⭕️ SUCCESS): STOMP 메시지가 성공적으로 전송되었습니다.")
	void saveAndSendChatMessage_void_success() {
		// GIVEN
		Long chatRoomId = 1L;

		AuthUser authUser = MemberFixture.createAuthUserEntity();
		ChatMessageCreateDto chatMessageCreateDto = ChatFixture.createChatMessageEntityForChatMessageCreateDto();

		Map<String, Object> sessionAttributes = new HashMap<>();
		sessionAttributes.put("authUser", authUser);

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
}
