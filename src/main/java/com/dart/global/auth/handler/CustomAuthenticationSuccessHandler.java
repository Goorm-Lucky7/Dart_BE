package com.dart.global.auth.handler;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.dart.api.application.auth.JwtProviderService;
import com.dart.api.domain.auth.entity.CustomOAuth2User;
import com.dart.api.domain.member.entity.Member;
import com.dart.api.dto.auth.response.TokenResDto;
import com.dart.global.common.util.CookieUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

	private final ObjectMapper objectMapper;
	private final JwtProviderService jwtProviderService;
	private final CookieUtil cookieUtil;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) throws IOException {

		CustomOAuth2User customOAuth2User = (CustomOAuth2User)authentication.getPrincipal();

		Member member = customOAuth2User.getMember();
		TokenResDto tokenResDto = new TokenResDto(generateAccessToken(member, request));
		generateRefreshTokenAndCookie(member, response);

		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(objectMapper.writeValueAsString(tokenResDto));

		if(customOAuth2User.isNewUser()) response.setStatus(HttpServletResponse.SC_CREATED);
		else response.setStatus(HttpServletResponse.SC_OK);
	}

	private String generateAccessToken(Member member, HttpServletRequest request) {
		return jwtProviderService.generateAccessToken(member.getId(), member.getEmail(), member.getNickname(),
			member.getProfileImageUrl(), extractClientInfo(request));
	}

	private String extractClientInfo(HttpServletRequest request) {
		String ipAddress = request.getRemoteAddr();
		String userAgent = request.getHeader("User-Agent");
		return ipAddress + "|" + userAgent;
	}

	private void generateRefreshTokenAndCookie(Member member, HttpServletResponse response) {
		String refreshToken = jwtProviderService.generateRefreshToken(member.getEmail());
		cookieUtil.setRefreshCookie(response, refreshToken);
	}
}