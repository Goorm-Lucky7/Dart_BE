package com.dart.api.infrastructure.websocket;

import java.nio.charset.StandardCharsets;

import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;

import com.dart.global.error.exception.UnauthorizedException;
import com.dart.global.error.model.ErrorCode;

import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class WebSocketErrorHandler extends StompSubProtocolErrorHandler {

	public WebSocketErrorHandler() {
		super();
	}

	@Override
	public Message<byte[]> handleClientMessageProcessingError(Message<byte[]> clientMessage, Throwable throwable) {
		Throwable exceptionCause = throwable.getCause();

		if (exceptionCause instanceof JwtException) {
			log.warn("[✅LOGGER] HANDLING EXPIRED JWT EXCEPTION: {}", throwable.getMessage());
			return handleJwtException(clientMessage, throwable);
		}

		if (exceptionCause instanceof UnauthorizedException) {
			log.warn("[✅LOGGER] HANDLING MESSAGE DELIVERY EXCEPTION: {}", throwable.getMessage());
			log.warn("[✅LOGGER] MESSAGE DELIVERY EXCEPTION DESCRIPTION: {}", throwable.getCause().getMessage());
			return handleUnauthorizedException(clientMessage, throwable);
		}

		log.error("[✅LOGGER] UNHANDLED EXCEPTION: {}", throwable.getMessage(), throwable);
		return super.handleClientMessageProcessingError(clientMessage, throwable);
	}

	private Message<byte[]> handleUnauthorizedException(Message<byte[]> clientMessage, Throwable throwable) {
		return prepareErrorMessage(ErrorCode.FAIL_LOGIN_REQUIRED);
	}

	private Message<byte[]> handleJwtException(Message<byte[]> clientMessage, Throwable throwable) {
		return prepareErrorMessage(ErrorCode.FAIL_TOKEN_EXPIRED);
	}

	private Message<byte[]> prepareErrorMessage(ErrorCode errorCode) {
		StompHeaderAccessor stompHeaderAccessor = StompHeaderAccessor.create(StompCommand.ERROR);

		stompHeaderAccessor.setMessage(errorCode.getMessage());
		stompHeaderAccessor.setLeaveMutable(true);

		return MessageBuilder.createMessage(
			errorCode.getMessage().getBytes(StandardCharsets.UTF_8),
			stompHeaderAccessor.getMessageHeaders());
	}
}

