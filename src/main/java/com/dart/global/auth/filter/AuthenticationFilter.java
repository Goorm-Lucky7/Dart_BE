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
import com.dart.global.error.exception.UnauthorizedException;
import com.dart.global.error.model.ErrorCode;

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

	private final JwtProviderService jwtProviderService;
	private final HandlerExceptionResolver handlerExceptionResolver;

	public AuthenticationFilter(
		JwtProviderService jwtProviderService,
		@Qualifier("handlerExceptionResolver") HandlerExceptionResolver handlerExceptionResolver
	) {
		this.jwtProviderService = jwtProviderService;
		this.handlerExceptionResolver = handlerExceptionResolver;
	}

	@Override
	protected void doFilterInternal(
		@NotNull HttpServletRequest request,
		@NotNull HttpServletResponse response,
		@NotNull FilterChain filterChain
	) {
		String requestURI = request.getRequestURI();

		if (requestURI.startsWith(WEBSOCKET_PATH_PREFIX) || requestURI.startsWith(REISSUE_PATH_PREFIX)) {
			try {
				filterChain.doFilter(request, response);
			} catch (IOException | ServletException e) {
				throw new RuntimeException(e);
			}
			return;
		}

		try {
			String accessToken = jwtProviderService.extractToken(ACCESS_TOKEN_HEADER, request);

			if (accessToken == null) {
				AuthorizationThreadLocal.setAuthUser(null);
				filterChain.doFilter(request, response);
			} else if (jwtProviderService.isUsable(accessToken)) {
				String email = jwtProviderService.extractEmailFromToken(accessToken);
				if (jwtProviderService.isAccessToken(email)) {
					setAuthentication(accessToken);
					filterChain.doFilter(request, response);
				}
			} else {
				throw new UnauthorizedException(ErrorCode.FAIL_TOKEN_EXPIRED);
			}
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			handlerExceptionResolver.resolveException(request, response, null, e);
		} finally {
			AuthorizationThreadLocal.remove();
		}
	}

	private void setAuthentication(String accessToken) {
		final AuthUser authUser = jwtProviderService.extractAuthUserByAccessToken(accessToken);
		final Authentication authToken = new UsernamePasswordAuthenticationToken(authUser, BLANK, null);

		SecurityContextHolder.getContext().setAuthentication(authToken);
		AuthorizationThreadLocal.setAuthUser(authUser);
	}
}
