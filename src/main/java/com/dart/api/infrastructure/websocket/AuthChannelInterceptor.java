package com.dart.api.infrastructure.websocket;

import static com.dart.global.common.util.AuthConstant.*;
import static com.dart.global.common.util.ChatConstant.*;

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
public class AuthChannelInterceptor implements ChannelInterceptor {

	private final JwtProviderService jwtProviderService;

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaderAccessor stompHeaderAccessor = StompHeaderAccessor.wrap(message);

		if (stompHeaderAccessor.getCommand() == StompCommand.CONNECT) {
			String authorizationHeader = stompHeaderAccessor.getFirstNativeHeader(ACCESS_TOKEN_HEADER);
			log.info("[✅ LOGGER] AUTHORIZATION HEADER: {}", authorizationHeader);
			if (!this.validateAccessToken(authorizationHeader, stompHeaderAccessor)) {
				log.error("[✅ LOGGER] ACCESS TOKEN IS EMPTIED OR EXPIRED");
			}
		}

		return message;
	}

	private boolean validateAccessToken(String accessToken, StompHeaderAccessor stompHeaderAccessor) {
		if (accessToken == null) {
			return false;
		}

		String token = accessToken.trim();

		if (!token.trim().isEmpty() && token.startsWith("Bearer ")) {
			accessToken = token.substring(7);
		}

		if (!jwtProviderService.isUsable(accessToken)) {
			return false;
		}

		AuthUser authUser = jwtProviderService.extractAuthUserByAccessToken(accessToken);
		stompHeaderAccessor.getSessionAttributes().put(CHAT_SESSION_USER, authUser);

		return true;
	}
}
