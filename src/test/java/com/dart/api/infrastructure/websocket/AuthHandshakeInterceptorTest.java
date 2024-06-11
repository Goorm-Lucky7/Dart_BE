package com.dart.api.infrastructure.websocket;

import static com.dart.global.common.util.AuthConstant.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;

import com.dart.api.application.auth.JwtProviderService;
import com.dart.api.domain.auth.entity.AuthUser;
import com.dart.support.MemberFixture;

import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
class AuthHandshakeInterceptorTest {

	@Mock
	private JwtProviderService jwtProviderService;

	@Mock
	private ServletServerHttpRequest servletServerHttpRequest;

	@Mock
	private ServletServerHttpResponse servletServerHttpResponse;

	@Mock
	private WebSocketHandler webSocketHandler;

	@Mock
	private HttpServletRequest httpServletRequest;

	@InjectMocks
	private AuthHandshakeInterceptor authHandshakeInterceptor;

	private Map<String, Object> attributes;

	@BeforeEach
	void setUp() {
		attributes = new HashMap<>();
	}

	@Test
	@DisplayName("BEFORE HANDSHAKE(⭕️ SUCCESS): 해당 요청은 유효한 JWT 토큰입니다.")
	void beforeHandshake_valid_token_success() {
		// GIVEN
		String accessToken = "testAccessToken";
		AuthUser authUser = MemberFixture.createAuthUserEntity();

		given(servletServerHttpRequest.getServletRequest()).willReturn(httpServletRequest);
		given(jwtProviderService.extractToken(ACCESS_TOKEN_HEADER, httpServletRequest)).willReturn(accessToken);
		given(jwtProviderService.isUsable(accessToken)).willReturn(true);
		given(jwtProviderService.extractAuthUserByAccessToken(accessToken)).willReturn(authUser);

		// WHEN
		boolean actual = authHandshakeInterceptor
			.beforeHandshake(servletServerHttpRequest, servletServerHttpResponse, webSocketHandler, attributes);

		// THEN
		verify(jwtProviderService, times(1)).extractToken(ACCESS_TOKEN_HEADER, httpServletRequest);
		verify(jwtProviderService, times(1)).isUsable(accessToken);
		verify(jwtProviderService, times(1)).extractAuthUserByAccessToken(accessToken);

		assertThat(actual).isTrue();
		assertThat(attributes).containsEntry("authUser", authUser);
	}

	@Test
	@DisplayName("BEFORE HANDSHAKE(❌ FAILURE): JWT 토큰이 없는 경우")
	void beforeHandshake_noToken_fail() {
		// GIVEN
		given(servletServerHttpRequest.getServletRequest()).willReturn(httpServletRequest);
		given(jwtProviderService.extractToken(ACCESS_TOKEN_HEADER, httpServletRequest)).willReturn(null);

		// WHEN
		boolean actual = authHandshakeInterceptor.beforeHandshake(servletServerHttpRequest, servletServerHttpResponse,
			webSocketHandler, attributes);

		// THEN
		verify(jwtProviderService, times(1)).extractToken(ACCESS_TOKEN_HEADER, httpServletRequest);
		verify(jwtProviderService, times(0)).isUsable(null);
		verify(jwtProviderService, times(0)).extractAuthUserByAccessToken(null);
		assertThat(actual).isFalse();
		verify(servletServerHttpResponse).setStatusCode(HttpStatus.UNAUTHORIZED);
	}

	@Test
	@DisplayName("BEFORE HANDSHAKE(❌ FAILURE): 유효하지 않은 JWT 토큰인 경우")
	void beforeHandshake_invalidToken_fail() {
		// GIVEN
		String accessToken = "invalidToken";

		given(servletServerHttpRequest.getServletRequest()).willReturn(httpServletRequest);
		given(jwtProviderService.extractToken(ACCESS_TOKEN_HEADER, httpServletRequest)).willReturn(accessToken);
		given(jwtProviderService.isUsable(accessToken)).willReturn(false);

		// WHEN
		boolean actual = authHandshakeInterceptor.beforeHandshake(servletServerHttpRequest, servletServerHttpResponse,
			webSocketHandler, attributes);

		// THEN
		verify(jwtProviderService, times(1)).extractToken(ACCESS_TOKEN_HEADER, httpServletRequest);
		verify(jwtProviderService, times(1)).isUsable(accessToken);
		verify(jwtProviderService, times(0)).extractAuthUserByAccessToken(accessToken);
		assertThat(actual).isFalse();
		verify(servletServerHttpResponse).setStatusCode(HttpStatus.UNAUTHORIZED);
	}
}
