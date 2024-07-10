package com.dart.global.auth.handler;

import static com.dart.global.common.util.GlobalConstant.*;

import java.io.IOException;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.dart.api.domain.auth.entity.CustomOAuth2User;
import com.dart.api.domain.auth.repository.SessionRedisRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

	private final SessionRedisRepository sessionRedisRepository;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) throws IOException {

		CustomOAuth2User customOAuth2User = (CustomOAuth2User)authentication.getPrincipal();

		String email = customOAuth2User.getEmail();
		String sessionId = UUID.randomUUID().toString();
		sessionRedisRepository.saveSessionLoginMapping(sessionId, email);

		String redirectUrl = UriComponentsBuilder.fromUriString("https://www." + DEPLOY_DOMAIN)
			.path("/api/oauth2/callback")
			.queryParam("session-id", sessionId)
			.build().toUriString();

		try {
			response.sendRedirect(redirectUrl);
		} catch (IOException e) {
			response.sendRedirect(redirectUrl);
		}
	}
}
