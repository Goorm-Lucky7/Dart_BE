package com.dart.api.infrastructure.websocket;

import static com.dart.global.common.util.AuthConstant.*;
import static com.dart.global.common.util.ChatConstant.*;

import java.util.HashMap;

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

		if (isConnectCommand(stompHeaderAccessor)) {
			String authorizationHeader = stompHeaderAccessor.getFirstNativeHeader(ACCESS_TOKEN_HEADER);
			if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER + " ")) {
				log.warn("[✅ LOGGER] INVALID OR MISSING AUTHORIZATION HEADER");
				throw new NotFoundException(ErrorCode.FAIL_TOKEN_NOT_FOUND);
			}

			String accessToken = extractToken(authorizationHeader);
			validateAccessToken(accessToken);

			AuthUser authUser = jwtProviderService.extractAuthUserByAccessToken(accessToken);

			stompHeaderAccessor.getSessionAttributes().computeIfAbsent(CHAT_SESSION_USER, key -> new HashMap<>());
			stompHeaderAccessor.getSessionAttributes().put(CHAT_SESSION_USER, authUser);
		}

		return message;
	}

	private boolean isConnectCommand(StompHeaderAccessor stompHeaderAccessor) {
		return StompCommand.CONNECT.equals(stompHeaderAccessor.getCommand());
	}

	private String extractToken(String authorizationHeader) {
		return authorizationHeader.substring((BEARER + " ").length()).trim();
	}

	private void validateAccessToken(String accessToken) {
		if (accessToken.isEmpty()) {
			log.warn("[✅ LOGGER] TOKEN IS EMPTY");
			throw new NotFoundException(ErrorCode.FAIL_TOKEN_NOT_FOUND);
		}

		if (!jwtProviderService.isUsable(accessToken)) {
			log.warn("[✅ LOGGER] JWT TOKEN IS NOT USABLE");
			throw new NotFoundException(ErrorCode.FAIL_INVALID_TOKEN);
		}
	}
}
