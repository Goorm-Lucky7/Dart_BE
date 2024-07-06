package com.dart.global.auth.handler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import com.dart.global.error.model.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

	private final ObjectMapper objectMapper;
	private static final Logger logger = LoggerFactory.getLogger(CustomAuthenticationFailureHandler.class);

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
		AuthenticationException exception) throws IOException {

		ErrorCode errorCode = determineErrorCode(exception);
		int httpStatus = mapToHttpStatus(errorCode);

		response.setStatus(httpStatus);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		Map<String, String> errorDetails = new HashMap<>();
		errorDetails.put("errorCode", errorCode.name());
		errorDetails.put("errorMessage", errorCode.getMessage());

		response.getWriter().write(objectMapper.writeValueAsString(errorDetails));
		response.getWriter().flush();
	}

	private ErrorCode determineErrorCode(AuthenticationException exception) {
		if (exception instanceof OAuth2AuthenticationException) {
			OAuth2Error error = ((OAuth2AuthenticationException) exception).getError();
			if ("invalid_token".equals(error.getErrorCode()) || "expired_token".equals(error.getErrorCode())) {
				return ErrorCode.FAIL_INVALID_TOKEN;
			}
		}
		return ErrorCode.FAIL_INTERNAL_SERVER_ERROR;
	}

	private int mapToHttpStatus(ErrorCode errorCode) {
		switch (errorCode) {
			case FAIL_INVALID_TOKEN:
				return HttpServletResponse.SC_UNAUTHORIZED;
			default:
				return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
		}
	}
}