package com.dart.global.common.util;

import static com.dart.global.common.util.AuthConstant.*;
import static com.dart.global.common.util.GlobalConstant.COOKIE_DOMAIN;

import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CookieUtil {

	public void setCookie(HttpServletResponse response, String name, String value, long maxAgeSeconds){
		ResponseCookie cookie = ResponseCookie.from(name, value)
			.httpOnly(true)
			.secure(true)
			.path("/")
			.maxAge(maxAgeSeconds)
			.sameSite("None")
			.domain(COOKIE_DOMAIN)
			.build();

		response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
	}

	public void setRefreshCookie(HttpServletResponse response, String refreshToken) {
		setCookie(response, REFRESH_TOKEN_COOKIE_NAME, refreshToken, REFRESH_TOKEN_EXPIRATION_TIME_SECONDS);
	}

	public void setSessionCookie(HttpServletResponse response) {
		String sessionId = UUID.randomUUID().toString();
		setCookie(response, SESSION_ID, sessionId, SESSION_EXPIRATION_TIME_SECONDS);
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
}
