package com.dart.api.infrastructure.websocket;

import static com.dart.global.common.util.AuthConstant.*;
import static com.dart.global.common.util.ChatConstant.*;
import static com.dart.global.common.util.GlobalConstant.*;

import java.util.HashMap;
import java.util.Objects;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import com.dart.api.application.auth.JwtProviderService;
import com.dart.api.domain.auth.entity.AuthUser;
import com.dart.global.error.exception.UnauthorizedException;
import com.dart.global.error.model.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthChannelInterceptor implements ChannelInterceptor {

	private final JwtProviderService jwtProviderService;

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaderAccessor stompHeaderAccessor = StompHeaderAccessor.wrap(message);

		if (!isSendCommand(stompHeaderAccessor)) {
			return message;
		}

		final String authorizationHeader = stompHeaderAccessor.getFirstNativeHeader(ACCESS_TOKEN_HEADER);
		final String token = extractToken(authorizationHeader);
		validateAuthenticateToken(token);

		final AuthUser authUser = jwtProviderService.extractAuthUserByAccessToken(token);
		if (stompHeaderAccessor.getSessionAttributes() == null) {
			stompHeaderAccessor.setSessionAttributes(new HashMap<>());
		}
		stompHeaderAccessor.getSessionAttributes().put(CHAT_SESSION_USER, authUser);

		return message;
	}

	private boolean isSendCommand(StompHeaderAccessor stompHeaderAccessor) {
		return Objects.equals(StompCommand.SEND, stompHeaderAccessor.getHeader(STOMP_COMMAND_HEADER));
	}

	private String extractToken(String authorizationHeader) {
		if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER)) {
			return null;
		}
		return authorizationHeader.replaceFirst(BEARER, BLANK).trim();
	}

	private void validateAuthenticateToken(String token) {
		if (token == null || !jwtProviderService.isUsable(token)) {
			log.warn("[✅ LOGGER] INVALID OR MISSING JWT TOKEN");
			throw new UnauthorizedException(ErrorCode.FAIL_TOKEN_EXPIRED_OR_INVALID);
		}
	}
}
