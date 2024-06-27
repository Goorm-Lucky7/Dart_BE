package com.dart.api.infrastructure.websocket;

import static com.dart.global.common.util.AuthConstant.*;
import static com.dart.global.common.util.ChatConstant.*;
import static com.dart.global.common.util.GlobalConstant.*;

import java.util.HashMap;
import java.util.Objects;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import com.dart.api.application.auth.JwtProviderService;
import com.dart.api.domain.auth.entity.AuthUser;
import com.dart.global.error.exception.NotFoundException;
import com.dart.global.error.model.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class AuthChannelInterceptor implements ChannelInterceptor {

	private final JwtProviderService jwtProviderService;

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaderAccessor stompHeaderAccessor = StompHeaderAccessor.wrap(message);

		log.info("[✅ LOGGER] STOMP Command: {}", stompHeaderAccessor.getCommand());

		if (isConnectCommand(stompHeaderAccessor)) {
			final String authorizationHeader = stompHeaderAccessor.getFirstNativeHeader(ACCESS_TOKEN_HEADER);
			final String accessToken = extractToken(authorizationHeader);
			validateAuthenticateToken(accessToken);

			final AuthUser authUser = jwtProviderService.extractAuthUserByAccessToken(accessToken);
			if (stompHeaderAccessor.getSessionAttributes() == null) {
				stompHeaderAccessor.setSessionAttributes(new HashMap<>());
			}
			stompHeaderAccessor.getSessionAttributes().put(CHAT_SESSION_USER, authUser);

			stompHeaderAccessor.getSessionAttributes().forEach((key, value) -> {
				log.info("[✅ LOGGER] Session Attribute - Key: {}, Value: {}", key, value);
			});
		}

		return message;
	}

	private boolean isConnectCommand(StompHeaderAccessor stompHeaderAccessor) {
		return Objects.equals(StompCommand.CONNECT, stompHeaderAccessor.getCommand());
	}

	private String extractToken(String authorizationHeader) {
		if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER)) {
			return null;
		}
		return authorizationHeader.replaceFirst(BEARER, BLANK).trim();
	}

	private void validateAuthenticateToken(String accessToken) {
		if (accessToken == null || !jwtProviderService.isUsable(accessToken)) {
			log.warn("[✅ LOGGER] INVALID OR MISSING JWT TOKEN");
			throw new NotFoundException(ErrorCode.FAIL_INVALID_TOKEN);
		}
	}
}
