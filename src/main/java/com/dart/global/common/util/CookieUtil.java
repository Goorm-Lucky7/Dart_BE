package com.dart.global.common.util;

import static com.dart.global.common.util.AuthConstant.*;
import static com.dart.global.common.util.GlobalConstant.*;

import java.util.UUID;

import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CookieUtil {

	private final Environment env;

	public void setCookie(HttpServletResponse response, String name, String value, long maxAgeSeconds){
		boolean isLocal = isLocalActiveProfile();
		String domain = isLocal ? LOCAL_DOMAIN : COOKIE_DOMAIN;

		ResponseCookie.ResponseCookieBuilder cookieBuilder = ResponseCookie.from(name, value)
			.httpOnly(true)
			.path("/")
			.maxAge(maxAgeSeconds)
			.sameSite("None")
			.domain(domain);

		if(!isLocal) {
			cookieBuilder.secure(true);
		}

		ResponseCookie cookie = cookieBuilder.build();
		response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
	}

	public void setRefreshCookie(HttpServletResponse response, String refreshToken) {
		setCookie(response, REFRESH_TOKEN_COOKIE_NAME, refreshToken, REFRESH_TOKEN_EXPIRATION_TIME_SECONDS);
	}

	public String setSessionCookie(HttpServletResponse response) {
		String sessionId = UUID.randomUUID().toString();
		setCookie(response, SESSION_ID, sessionId, SESSION_EXPIRATION_TIME_SECONDS);
		return sessionId;
	}

	public String getCookie(HttpServletRequest request, String name) {
		if (request.getCookies() != null) {
			for (Cookie cookie : request.getCookies()) {
				if (cookie.getName().equals(name)) {
					return cookie.getValue();
				}
			}
		}
		return null;
	}

	private boolean isLocalActiveProfile() {
		for (String profile : env.getActiveProfiles()) {
			if ("local".equals(profile)) {
				return true;
			}
		}
		return false;
	}
}
