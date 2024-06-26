package com.dart.api.infrastructure.websocket;

import static com.dart.global.common.util.AuthConstant.*;
import static com.dart.global.common.util.GlobalConstant.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;

import com.dart.api.application.auth.JwtProviderService;
import com.dart.api.domain.auth.entity.AuthUser;
import com.dart.support.MemberFixture;

@ExtendWith(MockitoExtension.class)
class AuthChannelInterceptorTest {

	@Mock
	private JwtProviderService jwtProviderService;

	@Mock
	private MessageChannel messageChannel;

	@InjectMocks
	private AuthChannelInterceptor authChannelInterceptor;

	@Test
	@DisplayName("PRE SEND(⭕️ SUCCESS): 유효한 JWT 토큰이 제공되어 성공적으로 메시지를 전송했습니다.")
	void preSend_void_success() {
		// GIVEN
		String accessToken = "testValidAccessToken";
		AuthUser authUser = MemberFixture.createAuthUserEntity();

		StompHeaderAccessor stompHeaderAccessor = StompHeaderAccessor.create(StompCommand.CONNECT);
		stompHeaderAccessor.setNativeHeader(ACCESS_TOKEN_HEADER, BEARER + BLANK + accessToken);
		stompHeaderAccessor.setSessionAttributes(new HashMap<>());

		Message<?> message = MessageBuilder.withPayload(new byte[0]).setHeaders(stompHeaderAccessor).build();

		when(jwtProviderService.isUsable(anyString())).thenReturn(true);
		when(jwtProviderService.extractAuthUserByAccessToken(anyString())).thenReturn(authUser);

		// WHEN
		Message<?> actualMessage = authChannelInterceptor.preSend(message, messageChannel);

		// THEN
		assertEquals(message, actualMessage);
		verify(jwtProviderService, times(1)).isUsable(anyString());
		verify(jwtProviderService, times(1)).extractAuthUserByAccessToken(anyString());
	}

	@Test
	@DisplayName("PRE SEND(❌ FAILURE): JWT 토큰이 유효하지 않아서 메시지 전송에 실패했습니다.")
	void preSend_InvalidAccessTokenException_fail() {
		// GIVEN
		String accessToken = "testValidAccessToken";

		StompHeaderAccessor stompHeaderAccessor = StompHeaderAccessor.create(StompCommand.CONNECT);
		stompHeaderAccessor.setNativeHeader(ACCESS_TOKEN_HEADER, BEARER + BLANK + accessToken);
		stompHeaderAccessor.setSessionAttributes(new HashMap<>());

		Message<?> message = MessageBuilder.withPayload(new byte[0]).setHeaders(stompHeaderAccessor).build();

		when(jwtProviderService.isUsable(anyString())).thenReturn(false);

		// WHEN
		Message<?> actualMessage = authChannelInterceptor.preSend(message, messageChannel);

		// THEN
		assertEquals(message, actualMessage);
		verify(jwtProviderService, times(1)).isUsable(anyString());
		verify(jwtProviderService, never()).extractAuthUserByAccessToken(anyString());
	}

	@Test
	@DisplayName("PRE SEND(❌ FAILURE): JWT 토큰이 유효하지 않아서 메시지 전송에 실패했습니다.")
	void preSend_AuthorizationHeaderEmptyOrNotBearerException_fail() {
		// GIVEN
		StompHeaderAccessor stompHeaderAccessor = StompHeaderAccessor.create(StompCommand.CONNECT);

		Message<?> message = MessageBuilder.withPayload(new byte[0]).setHeaders(stompHeaderAccessor).build();

		// WHEN
		Message<?> actualMessage = authChannelInterceptor.preSend(message, messageChannel);

		// THEN
		assertEquals(message, actualMessage);
		verify(jwtProviderService, never()).isUsable(anyString());
		verify(jwtProviderService, never()).extractAuthUserByAccessToken(anyString());
	}
}
