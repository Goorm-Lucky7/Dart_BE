package com.dart.api.infrastructure.websocket;

import static com.dart.global.common.util.AuthConstant.*;
import static com.dart.global.common.util.ChatConstant.*;

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

		log.info("MESSAGE: {}", message);
		log.info("HEADER: {}", stompHeaderAccessor.getMessageHeaders());
		log.info("ACCESS TOKEN: {}", stompHeaderAccessor.getNativeHeader(ACCESS_TOKEN_HEADER));

		if (StompCommand.CONNECT.equals(stompHeaderAccessor.getCommand())) {
			String accessToken = Objects
				.requireNonNull(stompHeaderAccessor.getFirstNativeHeader(ACCESS_TOKEN_HEADER))
				.substring(7);

			if (jwtProviderService.isUsable(accessToken)) {
				AuthUser authUser = jwtProviderService.extractAuthUserByAccessToken(accessToken);
				stompHeaderAccessor.setHeader(CHAT_SESSION_USER, authUser);
				log.info("[✅ LOGGER] USER AUTHORIZED: {}", authUser.nickname());
			} else {
				log.warn("[✅ LOGGER] UNAUTHORIZED ACCESS ATTEMPT OR INVALID TOKEN");
				throw new UnauthorizedException(ErrorCode.FAIL_LOGIN_REQUIRED);
			}
		}

		return message;
	}
}
