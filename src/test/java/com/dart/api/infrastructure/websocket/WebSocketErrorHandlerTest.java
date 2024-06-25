package com.dart.api.infrastructure.websocket;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;

import com.dart.global.error.exception.UnauthorizedException;
import com.dart.global.error.model.ErrorCode;

import io.jsonwebtoken.JwtException;

@ExtendWith(MockitoExtension.class)
class WebSocketErrorHandlerTest {

	@Mock
	private Message<byte[]> clientMessage;

	@InjectMocks
	private WebSocketErrorHandler webSocketErrorHandler;

	@Test
	@DisplayName("HANDLE CLIENT MESSAGE PROCESSING ERROR(⭕️ SUCCESS): 만료된 JWT 토큰 예외를 처리하여 클라이언트 메시지 처리 오류를 성공적으로 핸들링했습니다.")
	void handleClientMessageProcessingError_JwtException_success() {
		// GIVEN
		JwtException jwtException = new JwtException("[❎ ERROR] JWT TOKEN EXPIRED");
		Throwable throwable = new Throwable(jwtException);

		// WHEN
		Message<byte[]> errorMessage = webSocketErrorHandler.handleClientMessageProcessingError(
			clientMessage,
			throwable);

		// THEN
		StompHeaderAccessor stompHeaderAccessor = StompHeaderAccessor.wrap(errorMessage);
		assertEquals(StompCommand.ERROR, stompHeaderAccessor.getCommand());
		assertEquals(
			"[❎ ERROR] 인증 토큰이 만료되었습니다. 다시 로그인해 주세요.",
			new String(errorMessage.getPayload(), StandardCharsets.UTF_8));
	}

	@Test
	@DisplayName("HANDLE CLIENT MESSAGE PROCESSING ERROR(⭕️ SUCCESS): 인증되지 않은 예외를 처리하여 클라이언트 메시지 처리 오류를 성공적으로 핸들링했습니다.")
	void handleClientMessageProcessingError_UnauthorizedException_success() {
		// GIVEN
		UnauthorizedException unauthorizedException = new UnauthorizedException(ErrorCode.FAIL_LOGIN_REQUIRED);
		Throwable throwable = new Throwable(unauthorizedException);

		// WHEN
		Message<byte[]> errorMessage = webSocketErrorHandler.handleClientMessageProcessingError(
			clientMessage,
			throwable);

		// THEN
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(errorMessage);
		assertEquals(StompCommand.ERROR, accessor.getCommand());
		assertEquals(
			"[❎ ERROR] 로그인이 필요한 기능입니다.",
			new String(errorMessage.getPayload(), StandardCharsets.UTF_8));
	}

	@Test
	@DisplayName("HANDLE CLIENT MESSAGE PROCESSING ERROR(⭕️ SUCCESS): 예상치 못한 예외를 처리하여 클라이언트 메시지 처리 오류를 성공적으로 핸들링했습니다.")
	void handleClientMessageProcessingError_RuntimeException_success() {
		// GIVEN
		RuntimeException unexpectedException = new RuntimeException("[❎ ERROR] UNEXPECTED ERROR");
		Throwable throwable = new Throwable(unexpectedException);

		when(clientMessage.getHeaders()).thenReturn(new MessageHeaders(null));

		// WHEN
		Message<byte[]> errorMessage = webSocketErrorHandler.handleClientMessageProcessingError(
			clientMessage,
			throwable);

		// THEN
		assertNotNull(errorMessage, "ERROR MESSAGE SHOULD NOT BE NULL");

		StompHeaderAccessor stompHeaderAccessor = StompHeaderAccessor.wrap(errorMessage);
		assertEquals(StompCommand.ERROR, stompHeaderAccessor.getCommand());
	}
}
