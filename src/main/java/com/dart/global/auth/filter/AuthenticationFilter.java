package com.dart.global.auth.filter;

import static com.dart.global.common.util.AuthConstant.*;
import static com.dart.global.common.util.GlobalConstant.*;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import com.dart.api.application.auth.JwtProviderService;
import com.dart.api.domain.auth.entity.AuthUser;
import com.dart.global.auth.AuthorizationThreadLocal;
import com.dart.global.common.util.CookieUtil;
import com.dart.global.error.exception.UnauthorizedException;
import com.dart.global.error.model.ErrorCode;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuthenticationFilter extends OncePerRequestFilter {

	private static final String REISSUE_PATH_PREFIX = "/api/reissue";
	private static final String WEBSOCKET_PATH_PREFIX = "/ws/";

	private final CookieUtil cookieUtil;
	private final JwtProviderService jwtProviderService;
	private final HandlerExceptionResolver handlerExceptionResolver;

	public AuthenticationFilter(
		CookieUtil cookieUtil, JwtProviderService jwtProviderService,
		@Qualifier("handlerExceptionResolver") HandlerExceptionResolver handlerExceptionResolver
	) {
		this.cookieUtil = cookieUtil;
		this.jwtProviderService = jwtProviderService;
		this.handlerExceptionResolver = handlerExceptionResolver;
	}

	@Override
	protected void doFilterInternal(
		@NotNull HttpServletRequest request,
		@NotNull HttpServletResponse response,
		@NotNull FilterChain filterChain
	) throws ServletException, IOException {
		String requestURI = request.getRequestURI();

		if (requestURI.startsWith(WEBSOCKET_PATH_PREFIX)) {
			proceedFilterChain(filterChain, request, response);
			return;
		}

		if (requestURI.startsWith(REISSUE_PATH_PREFIX)) {
			handleReissueRequest(request, response, filterChain);
			return;
		}

		String accessToken = jwtProviderService.extractToken(ACCESS_TOKEN_HEADER, request);
		String refreshToken = cookieUtil.getCookie(request, REFRESH_TOKEN_COOKIE_NAME);

		try {
			if (accessToken == null) {
				setUnauthenticatedAndProceed(filterChain, request, response);
				return;
			}

			if (jwtProviderService.isUsable(accessToken)) {
				setAuthentication(accessToken);
				proceedFilterChain(filterChain, request, response);
			} else {
				handleExpiredAccessToken(refreshToken, response, filterChain, request);
			}
		} catch (ExpiredJwtException e) {
			log.warn("Expired JWT token", e);
			handleExpiredAccessToken(refreshToken, response, filterChain, request);
		} catch (JwtException e) {
			log.error("Invalid JWT token", e);
			throw new UnauthorizedException(ErrorCode.FAIL_INVALID_TOKEN);
		} catch (Exception e) {
			log.error("Authentication error", e);
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			handlerExceptionResolver.resolveException(request, response, null, e);
		} finally {
			AuthorizationThreadLocal.remove();
		}
	}

	private void handleExpiredAccessToken(String refreshToken, HttpServletResponse response, FilterChain filterChain,
		HttpServletRequest request) throws IOException, ServletException {
		if (refreshToken != null && jwtProviderService.isUsable(refreshToken)) {
			String newAccessToken = jwtProviderService.reGenerateAccessToken(refreshToken);
			setAuthentication(newAccessToken);
			response.setHeader(ACCESS_TOKEN_HEADER, newAccessToken);
			proceedFilterChain(filterChain, request, response);
		} else {
			throw new UnauthorizedException(ErrorCode.FAIL_INVALID_TOKEN);
		}
	}

	private void handleReissueRequest(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
		String refreshToken = cookieUtil.getCookie(request, REFRESH_TOKEN_COOKIE_NAME);
		if (refreshToken != null) {
			try {
				String newAccessToken = jwtProviderService.reGenerateAccessToken(refreshToken);
				setAuthentication(newAccessToken);
				response.setHeader(ACCESS_TOKEN_HEADER, newAccessToken);
				proceedFilterChain(filterChain, request, response);
			} catch (Exception e) {
				throw new UnauthorizedException(ErrorCode.FAIL_INVALID_TOKEN);
			}
		} else {
			throw new UnauthorizedException(ErrorCode.FAIL_INVALID_TOKEN);
		}
	}

	private void setUnauthenticatedAndProceed(FilterChain filterChain, HttpServletRequest request,
		HttpServletResponse response) throws IOException, ServletException {
		AuthorizationThreadLocal.setAuthUser(null);
		proceedFilterChain(filterChain, request, response);
	}

	private void proceedFilterChain(FilterChain filterChain, HttpServletRequest request, HttpServletResponse response)
		throws IOException, ServletException {
		filterChain.doFilter(request, response);
	}

	private void setAuthentication(String accessToken) {
		final AuthUser authUser = jwtProviderService.extractAuthUserByAccessToken(accessToken);
		final Authentication authToken = new UsernamePasswordAuthenticationToken(authUser, BLANK, null);

		SecurityContextHolder.getContext().setAuthentication(authToken);
		AuthorizationThreadLocal.setAuthUser(authUser);
	}
}
