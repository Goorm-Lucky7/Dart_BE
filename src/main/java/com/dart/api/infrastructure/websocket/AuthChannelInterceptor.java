package com.dart.api.infrastructure.websocket;

import static com.dart.global.common.util.ChatConstant.*;

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
import com.dart.global.error.exception.UnauthorizedException;
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

		log.info("MESSAGE: {}", message);
		log.info("HEADER: {}", stompHeaderAccessor.getMessageHeaders());

		if (StompCommand.CONNECT.equals(stompHeaderAccessor.getCommand())) {
			String accessToken = getAccessTokenFromQuery(stompHeaderAccessor);

			if (accessToken != null && jwtProviderService.isUsable(accessToken)) {
				AuthUser authUser = jwtProviderService.extractAuthUserByAccessToken(accessToken);
				stompHeaderAccessor.setHeader(CHAT_SESSION_USER, authUser);
				log.info("[✅ LOGGER] USER AUTHORIZED: {}", authUser.nickname());
			} else {
				log.warn("[✅ LOGGER] TOKEN IS INVALID OR EXPIRED");
				throw new UnauthorizedException(ErrorCode.FAIL_LOGIN_REQUIRED);
			}
		}

		return message;
	}

	private String getAccessTokenFromQuery(StompHeaderAccessor stompHeaderAccessor) {
		String query = stompHeaderAccessor.getSessionAttributes().get(TOKEN_PARAM).toString();

		if (query != null && query.contains(TOKEN_PARAM + URL_QUERY_DELIMITER)) {
			String accessToken = query.split(TOKEN_PARAM + URL_QUERY_DELIMITER)[1];
			if (accessToken.contains(QUERY_PARAM_SEPARATOR)) {
				accessToken = accessToken.split(QUERY_PARAM_SEPARATOR)[0];
			}
			return accessToken;
		}

		return null;
	}
}
