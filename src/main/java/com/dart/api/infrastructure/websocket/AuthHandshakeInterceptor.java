package com.dart.api.infrastructure.websocket;

import static com.dart.global.common.util.ChatConstant.*;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.dart.api.application.auth.JwtProviderService;
import com.dart.api.domain.auth.entity.AuthUser;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class AuthHandshakeInterceptor implements HandshakeInterceptor {

	private final JwtProviderService jwtProviderService;

	@Override
	public boolean beforeHandshake(
		@NotNull ServerHttpRequest serverHttpRequest,
		@NotNull ServerHttpResponse serverHttpResponse,
		@NotNull WebSocketHandler webSocketHandler,
		@NotNull Map<String, Object> attributes
	) {
		log.info("[✅ LOGGER] START WEBSOCKET HANDSHAKE");

		if (serverHttpRequest instanceof ServletServerHttpRequest) {
			HttpServletRequest httpServletRequest = ((ServletServerHttpRequest)serverHttpRequest).getServletRequest();
			String token = extractTokenFromQuery(httpServletRequest);

			if (authenticateToken(token, attributes)) {
				return true;
			}

			log.warn("[❎ LOGGER] JWT TOKEN IS INVALID OR NOT PRESENT");
			serverHttpResponse.setStatusCode(HttpStatus.UNAUTHORIZED);

			return false;
		}

		return true;
	}

	@Override
	public void afterHandshake(
		@NotNull ServerHttpRequest serverHttpRequest,
		@NotNull ServerHttpResponse serverHttpResponse,
		@NotNull WebSocketHandler webSocketHandler,
		Exception exception
	) {
		log.info("[✅ LOGGER] WEBSOCKET HANDSHAKE COMPLETED");

		if (exception != null) {
			log.error("[❎ LOGGER] EXCEPTION DURING HANDSHAKE: ", exception);
		}
	}

	private String extractTokenFromQuery(HttpServletRequest httpServletRequest) {
		String query = httpServletRequest.getQueryString();

		if (query != null && query.contains(TOKEN_PARAM + URL_QUERY_DELIMITER)) {
			String token = query.split(TOKEN_PARAM + URL_QUERY_DELIMITER)[1];

			if (token.contains(QUERY_PARAM_SEPARATOR)) {
				token = token.split(QUERY_PARAM_SEPARATOR)[0];
			}

			return token;
		}

		return null;
	}

	private boolean authenticateToken(String token, Map<String, Object> attributes) {
		if (token != null && jwtProviderService.isUsable(token)) {
			AuthUser authUser = jwtProviderService.extractAuthUserByAccessToken(token);
			attributes.put(CHAT_SESSION_USER, authUser);
			log.info("[✅ LOGGER] SUCCESS MEMBER AUTHORIZATION: {}", authUser.nickname());

			return true;
		}
		return false;
	}
}
