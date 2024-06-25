package com.dart.api.infrastructure.websocket;

import static com.dart.global.common.util.AuthConstant.*;
import static com.dart.global.common.util.ChatConstant.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

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
import com.dart.global.error.exception.UnauthorizedException;
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
	@DisplayName("PRE SEND(⭕️ SUCCESS): 유효한 JWT 토큰이 제공되어 성공적으로 AuthUser를 설정했습니다.")
	void preSend_void_success() {
		// GIVEN
		String expectedValidToken = "Bearer expectedValidToken";
		String expectedAccessToken = expectedValidToken.substring(7);

		StompHeaderAccessor stompHeaderAccessor = StompHeaderAccessor.create(StompCommand.SEND);
		stompHeaderAccessor.addNativeHeader(ACCESS_TOKEN_HEADER, expectedValidToken);
		stompHeaderAccessor.setHeader(STOMP_COMMAND_HEADER, StompCommand.SEND);
		stompHeaderAccessor.setLeaveMutable(true);

		Message<byte[]> expectedMessage = MessageBuilder.createMessage(
			new byte[0],
			stompHeaderAccessor.getMessageHeaders());

		AuthUser authUser = MemberFixture.createAuthUserEntity();

		when(jwtProviderService.isUsable(expectedAccessToken)).thenReturn(true);
		when(jwtProviderService.extractAuthUserByAccessToken(expectedAccessToken)).thenReturn(authUser);

		// WHEN
		authChannelInterceptor.preSend(expectedMessage, messageChannel);

		// THEN
		verify(jwtProviderService).isUsable(expectedAccessToken);
		verify(jwtProviderService).extractAuthUserByAccessToken(expectedAccessToken);
	}

	@Test
	@DisplayName("PRE SEND(❌ FAILURE): JWT 토큰이 유효하지 않거나 누락되었습니다.")
	void preSend_UnauthorizedException_fail() {
		// GIVEN
		String expectedValidToken = "Bearer expectedValidToken";
		String expectedAccessToken = expectedValidToken.substring(7);

		StompHeaderAccessor stompHeaderAccessor = StompHeaderAccessor.create(StompCommand.SEND);
		stompHeaderAccessor.addNativeHeader(ACCESS_TOKEN_HEADER, expectedValidToken);
		stompHeaderAccessor.setHeader(STOMP_COMMAND_HEADER, StompCommand.SEND);
		stompHeaderAccessor.setLeaveMutable(true);

		Message<byte[]> invalidMessage = MessageBuilder.createMessage(
			new byte[0],
			stompHeaderAccessor.getMessageHeaders());

		when(jwtProviderService.isUsable(expectedAccessToken)).thenReturn(false);

		// WHEN & THEN
		assertThatThrownBy(() -> authChannelInterceptor.preSend(invalidMessage, messageChannel))
			.isInstanceOf(UnauthorizedException.class)
			.hasMessage("[❎ ERROR] 인증 토큰이 유효하지 않습니다. 다시 로그인해 주세요.");

		verify(jwtProviderService).isUsable(expectedAccessToken);
	}
}
