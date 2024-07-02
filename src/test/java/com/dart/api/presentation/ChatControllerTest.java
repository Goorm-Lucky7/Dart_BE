package com.dart.api.presentation;

import static com.dart.global.common.util.ChatConstant.*;
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

import com.dart.api.application.chat.ChatMessageService;
import com.dart.api.domain.auth.entity.AuthUser;
import com.dart.api.dto.chat.request.ChatMessageCreateDto;
import com.dart.api.dto.chat.response.MemberSessionDto;
import com.dart.api.infrastructure.websocket.MemberSessionRegistry;
import com.dart.support.AuthFixture;
import com.dart.support.ChatFixture;

@ExtendWith(SpringExtension.class)
class ChatControllerTest {

	@Mock
	private ChatMessageService chatMessageService;

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

		AuthUser authUser = AuthFixture.createAuthUserEntity();
		ChatMessageCreateDto chatMessageCreateDto = ChatFixture.createChatMessageEntityForChatMessageCreateDto();

		Map<String, Object> sessionAttributes = new HashMap<>();
		sessionAttributes.put(CHAT_SESSION_USER, authUser);

		given(simpMessageHeaderAccessor.getSessionAttributes()).willReturn(sessionAttributes);

		// WHEN
		chatController.saveAndSendChatMessage(chatRoomId, chatMessageCreateDto);

		// THEN
		verify(chatMessageService).saveChatMessage(chatRoomId, chatMessageCreateDto);
		verify(simpMessageSendingOperations).convertAndSend(TOPIC_PREFIX + chatRoomId, chatMessageCreateDto);
	}

	@Test
	@DisplayName("GET LOGGED-IN VISITORS(⭕️ SUCCESS): 성공적으로 채팅방에 로그인된 사용자 목록을 조회했습니다.")
	void getLoggedInVisitors_void_success() {
		// GIVEN
		Long chatRoomId = 1L;
		String destination = TOPIC_PREFIX + chatRoomId;
		List<MemberSessionDto> members = Arrays.asList(
			new MemberSessionDto("member1", "sessionId1", destination, "https://example.com/profile1.jpg"),
			new MemberSessionDto("member2", "sessionId2", destination, "https://example.com/profile2.jpg"),
			new MemberSessionDto("member3", "sessionId3", destination, "https://example.com/profile3.jpg")
		);

		given(memberSessionRegistry.getMembersInChatRoom(destination)).willReturn(members);

		// WHEN
		ResponseEntity<List<MemberSessionDto>> responseEntity = chatController.getLoggedInVisitors(chatRoomId);

		// THEN
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(responseEntity.getBody()).hasSize(3);
		assertThat(responseEntity.getBody()).containsExactlyInAnyOrderElementsOf(members);
	}

	@Test
	@DisplayName("GET LOGGED-IN VISITORS(⭕️ SUCCESS): 채팅방에 사용자가 없는 경우 빈 배열을 반환합니다.")
	void getLoggedInVisitors_empty_success() {
		// GIVEN
		Long chatRoomId = 1L;
		String destination = TOPIC_PREFIX + chatRoomId;

		given(memberSessionRegistry.getMembersInChatRoom(destination)).willReturn(List.of());

		// WHEN
		ResponseEntity<List<MemberSessionDto>> responseEntity = chatController.getLoggedInVisitors(chatRoomId);

		// THEN
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(responseEntity.getBody()).isEmpty();
	}
}
