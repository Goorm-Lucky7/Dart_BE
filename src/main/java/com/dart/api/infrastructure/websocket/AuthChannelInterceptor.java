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
import com.dart.global.error.exception.UnauthorizedException;
import com.dart.global.error.model.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthChannelInterceptor implements ChannelInterceptor {

	public static final String STOMP_COMMAND_HEADER = "stompCommand";

	private final JwtProviderService jwtProviderService;

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaderAccessor stompHeaderAccessor = StompHeaderAccessor.wrap(message);

		final String authorizationHeader = String.valueOf(
			stompHeaderAccessor.getFirstNativeHeader(ACCESS_TOKEN_HEADER));

		final String command = String.valueOf(
			stompHeaderAccessor.getHeader(STOMP_COMMAND_HEADER));

		if (!command.equals("SEND")) {
			return message;
		}

		if (authorizationHeader == null || authorizationHeader.equals("null")) {
			throw new UnauthorizedException(ErrorCode.FAIL_LOGIN_REQUIRED);
		}

		final String token = authorizationHeader.substring(7);

		try {
			jwtProviderService.isUsable(token);
			final AuthUser authUser = jwtProviderService.extractAuthUserByAccessToken(token);
			stompHeaderAccessor.setHeader(CHAT_SESSION_USER, authUser);
		} catch (Exception e) {
			log.error("FAIL VALIDATE JWT TOKEN: {}", e.getMessage(), e);
			throw new UnauthorizedException(ErrorCode.FAIL_INVALID_TOKEN);
		}

		return message;
	}
}
