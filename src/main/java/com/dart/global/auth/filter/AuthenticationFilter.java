package com.dart.global.auth.filter;

import static com.dart.global.common.util.GlobalConstant.*;
import static com.dart.global.common.util.AuthConstant.*;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import com.dart.api.domain.auth.entity.AuthUser;
import com.dart.global.auth.AuthorizationThreadLocal;
import com.dart.api.application.auth.JwtProviderService;
import com.dart.global.error.exception.UnauthorizedException;
import com.dart.global.error.model.ErrorCode;

@Slf4j
public class AuthenticationFilter extends OncePerRequestFilter {

	private static final String PATH_API_TOKEN_REISSUE = "/api/reissue";

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
		String accessToken = jwtProviderService.extractToken(ACCESS_TOKEN_HEADER, request);

		try {
			if (!jwtProviderService.isUsable(accessToken) || PATH_API_TOKEN_REISSUE.equals(requestURI)) {
				if (PATH_API_TOKEN_REISSUE.equals(requestURI)) {
					filterChain.doFilter(request, response);

					return;
				}

				if (jwtProviderService.isUsable(accessToken)) {
					String newAccessToken = jwtProviderService.reGenerateToken(accessToken);
					setAuthentication(newAccessToken);
				} else {
					log.info("Access Token not usable");
					AuthorizationThreadLocal.setAuthUser(null);
					filterChain.doFilter(request, response);

					return;
				}
			} else {
				setAuthentication(accessToken);
				filterChain.doFilter(request, response);

				return;
			}

			throw new UnauthorizedException(ErrorCode.FAIL_TOKEN_EXPIRED);
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
