package com.dart.api.infrastructure.websocket;

import static com.dart.global.common.util.ChatConstant.*;
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
		String query = TOKEN_PARAM + URL_QUERY_DELIMITER + accessToken;

		given(servletServerHttpRequest.getServletRequest()).willReturn(httpServletRequest);
		given(httpServletRequest.getQueryString()).willReturn(query);
		given(jwtProviderService.isUsable(accessToken)).willReturn(true);
		given(jwtProviderService.extractAuthUserByAccessToken(accessToken)).willReturn(authUser);

		// WHEN
		boolean actual = authHandshakeInterceptor
			.beforeHandshake(servletServerHttpRequest, servletServerHttpResponse, webSocketHandler, attributes);

		// THEN
		verify(jwtProviderService, times(1)).isUsable(accessToken);
		verify(jwtProviderService, times(1)).extractAuthUserByAccessToken(accessToken);

		assertThat(actual).isTrue();
		assertThat(attributes).containsEntry(CHAT_SESSION_USER, authUser);
	}

	@Test
	@DisplayName("BEFORE HANDSHAKE(❌ FAILURE): JWT 토큰이 없는 경우")
	void beforeHandshake_noToken_fail() {
		// GIVEN
		given(servletServerHttpRequest.getServletRequest()).willReturn(httpServletRequest);
		given(httpServletRequest.getQueryString()).willReturn(null);

		// WHEN
		boolean actual = authHandshakeInterceptor.beforeHandshake(servletServerHttpRequest, servletServerHttpResponse,
			webSocketHandler, attributes);

		// THEN
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
		String query = TOKEN_PARAM + URL_QUERY_DELIMITER + accessToken;

		given(servletServerHttpRequest.getServletRequest()).willReturn(httpServletRequest);
		given(httpServletRequest.getQueryString()).willReturn(query);
		given(jwtProviderService.isUsable(accessToken)).willReturn(false);

		// WHEN
		boolean actual = authHandshakeInterceptor.beforeHandshake(servletServerHttpRequest, servletServerHttpResponse,
			webSocketHandler, attributes);

		// THEN
		verify(jwtProviderService, times(1)).isUsable(accessToken);
		verify(jwtProviderService, times(0)).extractAuthUserByAccessToken(accessToken);
		assertThat(actual).isFalse();
		verify(servletServerHttpResponse).setStatusCode(HttpStatus.UNAUTHORIZED);
	}

	@Test
	@DisplayName("AFTER HANDSHAKE(⭕️ SUCCESS): 해당 요청의 핸드쉐이크가 성공적으로 완료되었습니다.")
	void afterHandshake_void_success() {
		// WHEN
		authHandshakeInterceptor.afterHandshake(servletServerHttpRequest, servletServerHttpResponse, webSocketHandler,
			null);

		// THEN
		verifyNoInteractions(servletServerHttpResponse);
	}

	@Test
	@DisplayName("AFTER HANDSHAKE(❌ FAILURE): 해당 요청의 핸드쉐이크 중 예외가 발생하였습니다.")
	void afterHandshake_Exception_fail() {
		// GIVEN
		Exception exception = new Exception("[❎ ERROR] EXCEPTION DURING HANDSHAKE");

		// WHEN
		authHandshakeInterceptor.afterHandshake(servletServerHttpRequest, servletServerHttpResponse, webSocketHandler,
			exception);

		// THEN
		verifyNoInteractions(servletServerHttpResponse);
	}
}
