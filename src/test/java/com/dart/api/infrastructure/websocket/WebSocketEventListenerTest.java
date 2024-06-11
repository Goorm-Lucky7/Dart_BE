package com.dart.api.infrastructure.websocket;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import com.dart.api.domain.auth.entity.AuthUser;
import com.dart.global.error.exception.BadRequestException;
import com.dart.global.error.exception.UnauthorizedException;
import com.dart.support.MemberFixture;

@ExtendWith(MockitoExtension.class)
class WebSocketEventListenerTest {

	@Mock
	private MemberSessionRegistry memberSessionRegistry;

	@Mock
	private SessionSubscribeEvent sessionSubscribeEvent;

	@Mock
	private SessionUnsubscribeEvent sessionUnsubscribeEvent;

	@Mock
	private SimpMessageHeaderAccessor simpMessageHeaderAccessor;

	@InjectMocks
	private WebSocketEventListener webSocketEventListener;

	@BeforeEach
	void setUp() {
		simpMessageHeaderAccessor = SimpMessageHeaderAccessor.create();
	}

	@Test
	@DisplayName("HANDLE SUBSCRIBE EVENT(⭕️ SUCCESS): 사용자가 성공적으로 채팅방에 가입되었습니다.")
	void handleSubscribeEvent_void_success() {
		// GIVEN
		String sessionId = "testSessionId";
		String chatRoomId = "1";
		String destination = "/sub/ws/" + chatRoomId;

		AuthUser authUser = MemberFixture.createAuthUserEntity();

		Map<String, Object> sessionAttributes = new HashMap<>();
		sessionAttributes.put("authUser", authUser);

		simpMessageHeaderAccessor.setSessionAttributes(sessionAttributes);
		simpMessageHeaderAccessor.setSessionId(sessionId);
		simpMessageHeaderAccessor.setDestination(destination);
		simpMessageHeaderAccessor.setLeaveMutable(true);

		Message<byte[]> message = MessageBuilder.withPayload(new byte[0])
			.copyHeaders(simpMessageHeaderAccessor.toMap())
			.build();

		given(sessionSubscribeEvent.getMessage()).willReturn(message);

		// WHEN
		webSocketEventListener.handleSubscribeEvent(sessionSubscribeEvent);

		// THEN
		verify(memberSessionRegistry, times(1)).addSession(authUser.nickname(), sessionId, destination);
	}

	@Test
	@DisplayName("HANDLE SUBSCRIBE EVENT(❌ FAILURE): 요청하신 채팅방에 세션 ID가 존재하지 않습니다.")
	void handleSubscribeEvent_sessionId_BadRequestException_fail() {
		// GIVEN
		String chatRoomId = "1";
		String destination = "/sub/ws/" + chatRoomId;

		AuthUser authUser = MemberFixture.createAuthUserEntity();

		Map<String, Object> sessionAttributes = new HashMap<>();
		sessionAttributes.put("authUser", authUser);

		simpMessageHeaderAccessor.setSessionAttributes(sessionAttributes);
		simpMessageHeaderAccessor.setDestination(destination);
		simpMessageHeaderAccessor.setLeaveMutable(true);

		Message<byte[]> message = MessageBuilder.withPayload(new byte[0])
			.copyHeaders(simpMessageHeaderAccessor.toMap())
			.build();

		given(sessionSubscribeEvent.getMessage()).willReturn(message);

		// WHEN & THEN
		assertThatThrownBy(() -> webSocketEventListener.handleSubscribeEvent(sessionSubscribeEvent))
			.isInstanceOf(BadRequestException.class)
			.hasMessage("[❎ ERROR] 요청하신 채팅방에 유효한 세션 ID가 필요합니다.");
	}

	@Test
	@DisplayName("HANDLE SUBSCRIBE EVENT(❌ FAILURE): 요청하신 채팅방의 대상이 존재하지 않습니다.")
	void handleSubscribeEvent_destination_BadRequestException_fail() {
		// GIVEN
		String sessionId = "testSessionId";

		AuthUser authUser = MemberFixture.createAuthUserEntity();

		Map<String, Object> sessionAttributes = new HashMap<>();
		sessionAttributes.put("authUser", authUser);

		simpMessageHeaderAccessor.setSessionAttributes(sessionAttributes);
		simpMessageHeaderAccessor.setSessionId(sessionId);
		simpMessageHeaderAccessor.setLeaveMutable(true);

		Message<byte[]> message = MessageBuilder.withPayload(new byte[0])
			.copyHeaders(simpMessageHeaderAccessor.toMap())
			.build();

		given(sessionSubscribeEvent.getMessage()).willReturn(message);

		// WHEN & THEN
		assertThatThrownBy(() -> webSocketEventListener.handleSubscribeEvent(sessionSubscribeEvent))
			.isInstanceOf(BadRequestException.class)
			.hasMessage("[❎ ERROR] 요청하신 채팅방의 대상이 유효하지 않습니다.");
	}

	@Test
	@DisplayName("HANDLE SUBSCRIBE EVENT(❌ FAILURE): 요청하신 채팅방은 인증된 사용자만 가능합니다.")
	void handleSubscribeEvent_authUser_UnauthorizedException_fail() {
		// GIVEN
		String sessionId = "testSessionId";
		String chatRoomId = "1";
		String destination = "/sub/ws/" + chatRoomId;

		Map<String, Object> sessionAttributes = new HashMap<>();

		simpMessageHeaderAccessor.setSessionAttributes(sessionAttributes);
		simpMessageHeaderAccessor.setSessionId(sessionId);
		simpMessageHeaderAccessor.setDestination(destination);
		simpMessageHeaderAccessor.setLeaveMutable(true);

		Message<byte[]> message = MessageBuilder.withPayload(new byte[0])
			.copyHeaders(simpMessageHeaderAccessor.toMap())
			.build();

		given(sessionSubscribeEvent.getMessage()).willReturn(message);

		// WHEN & THEN
		assertThatThrownBy(() -> webSocketEventListener.handleSubscribeEvent(sessionSubscribeEvent))
			.isInstanceOf(UnauthorizedException.class)
			.hasMessage("[❎ ERROR] 로그인이 필요한 기능입니다.");
	}

	@Test
	@DisplayName("HANDLE UNSUBSCRIBE EVENT(⭕️ SUCCESS): 사용자가 성공적으로 채팅방에서 탈퇴했습니다.")
	void handleUnsubscribeEvent_void_success() {
		// GIVEN
		String sessionId = "testSessionId";

		AuthUser authUser = MemberFixture.createAuthUserEntity();

		Map<String, Object> sessionAttributes = new HashMap<>();
		sessionAttributes.put("authUser", authUser);

		simpMessageHeaderAccessor.setSessionAttributes(sessionAttributes);
		simpMessageHeaderAccessor.setSessionId(sessionId);
		simpMessageHeaderAccessor.setLeaveMutable(true);

		Message<byte[]> message = MessageBuilder.withPayload(new byte[0])
			.copyHeaders(simpMessageHeaderAccessor.toMap())
			.build();

		given(sessionUnsubscribeEvent.getMessage()).willReturn(message);

		// WHEN
		webSocketEventListener.handleUnsubscribeEvent(sessionUnsubscribeEvent);

		// THEN
		verify(memberSessionRegistry, times(1)).removeSession(sessionId);
	}

	@Test
	@DisplayName("HANDLE UNSUBSCRIBE EVENT(❌ FAILURE): 요청하신 채팅방에 세션 ID가 존재하지 않습니다.")
	void handleUnsubscribeEvent_sessionId_BadRequestException_fail() {
		// GIVEN
		AuthUser authUser = MemberFixture.createAuthUserEntity();

		Map<String, Object> sessionAttributes = new HashMap<>();
		sessionAttributes.put("authUser", authUser);

		simpMessageHeaderAccessor.setSessionAttributes(sessionAttributes);
		simpMessageHeaderAccessor.setLeaveMutable(true);

		Message<byte[]> message = MessageBuilder.withPayload(new byte[0])
			.copyHeaders(simpMessageHeaderAccessor.toMap())
			.build();

		given(sessionUnsubscribeEvent.getMessage()).willReturn(message);

		// WHEN & THEN
		assertThatThrownBy(() -> webSocketEventListener.handleUnsubscribeEvent(sessionUnsubscribeEvent))
			.isInstanceOf(BadRequestException.class)
			.hasMessage("[❎ ERROR] 요청하신 채팅방에 유효한 세션 ID가 필요합니다.");
	}

	@Test
	@DisplayName("HANDLE UNSUBSCRIBE EVENT(❌ FAILURE): 요청하신 채팅방은 인증된 사용자만 가능합니다.")
	void handleUnsubscribeEvent_authUser_UnauthorizedException_fail() {
		// GIVEN
		String sessionId = "testSessionId";

		Map<String, Object> sessionAttributes = new HashMap<>();

		simpMessageHeaderAccessor.setSessionAttributes(sessionAttributes);
		simpMessageHeaderAccessor.setSessionId(sessionId);
		simpMessageHeaderAccessor.setLeaveMutable(true);

		Message<byte[]> message = MessageBuilder.withPayload(new byte[0])
			.copyHeaders(simpMessageHeaderAccessor.toMap())
			.build();

		given(sessionUnsubscribeEvent.getMessage()).willReturn(message);

		// WHEN & THEN
		assertThatThrownBy(() -> webSocketEventListener.handleUnsubscribeEvent(sessionUnsubscribeEvent))
			.isInstanceOf(UnauthorizedException.class)
			.hasMessage("[❎ ERROR] 로그인이 필요한 기능입니다.");
	}
}
