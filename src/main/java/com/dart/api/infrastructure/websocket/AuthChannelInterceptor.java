package com.dart.api.infrastructure.websocket;

import static com.dart.global.common.util.AuthConstant.*;
import static com.dart.global.common.util.ChatConstant.*;
import static com.dart.global.common.util.GlobalConstant.*;

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

		if (isSendCommand(stompHeaderAccessor)) {
			final String authorizationHeader = stompHeaderAccessor.getFirstNativeHeader(ACCESS_TOKEN_HEADER);
			final String accessToken = extractToken(authorizationHeader);

			if (!validateAuthenticateToken(accessToken, stompHeaderAccessor)) {
				log.error("[✅ LOGGER] ACCESS TOKEN IS EMPTIED OR EXPIRED");
			}
		}

		return message;
	}

	private boolean isSendCommand(StompHeaderAccessor stompHeaderAccessor) {
		return Objects.equals(StompCommand.CONNECT, stompHeaderAccessor.getCommand());
	}

	private String extractToken(String authorizationHeader) {
		if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER)) {
			return null;
		}
		return authorizationHeader.replaceFirst(BEARER, BLANK).trim();
	}

	private boolean validateAuthenticateToken(String accessToken, StompHeaderAccessor stompHeaderAccessor) {
		if (accessToken == null || !jwtProviderService.isUsable(accessToken)) {
			return false;
		}

		addAuthUserInSession(accessToken, stompHeaderAccessor);
		return true;
	}

	private void addAuthUserInSession(String accessToken, StompHeaderAccessor stompHeaderAccessor) {
		final AuthUser authUser = jwtProviderService.extractAuthUserByAccessToken(accessToken);
		stompHeaderAccessor.getSessionAttributes().put(CHAT_SESSION_USER, authUser);
	}
}
