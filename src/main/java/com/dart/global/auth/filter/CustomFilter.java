package com.dart.global.auth.filter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.dart.global.error.model.ErrorCode;
import com.dart.global.error.model.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;

@Component
@Order(1)
public class CustomFilter extends OncePerRequestFilter {

	private final ObjectMapper objectMapper = new ObjectMapper();

	private static final Map<String, List<String>> VALID_API_PATHS = Map.of(
		"POST", List.of(
			"/api/email.*",
			"/api/events.*",
			"/api/galleries",
			"/api/login",
			"/api/nickname/check",
			"/api/payment",
			"/api/reviews",
			"/api/signup.*"
		),
		"GET", List.of(
			"/api/email.*",
			"/api/chatroom.*",
			"/api/chatrooms.*",
			"/api/events.*",
			"/api/galleries.*",
			"/api/mypage.*",
			"/api/members.*",
			"/api/members.*",
			"/api/notifications.*",
			"/api/nickname/check",
			"/api/reissue",
			"/api/reviews.*",
			"/api/search.*"
		),
		"PUT", List.of(
			"/api/members"
		),
		"DELETE", List.of(
			"/api/galleries"
		)
	);

	@Override
	protected void doFilterInternal(
		@NotNull HttpServletRequest request,
		@NotNull HttpServletResponse response,
		@NotNull FilterChain filterChain)
		throws ServletException, IOException {

		if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
			response.setStatus(HttpServletResponse.SC_OK);
			return;
		}

		if (!isApiValid(request)) {
			response.setContentType("application/json;charset=UTF-8");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

			ErrorResponse errorResponse = new ErrorResponse(ErrorCode.FAIL_INVALID_REQUEST.getMessage());
			String jsonResponse = objectMapper.writeValueAsString(errorResponse);

			response.getWriter().write(jsonResponse);
			return;
		}

		filterChain.doFilter(request, response);
	}

	private boolean isApiValid(HttpServletRequest request) {
		String path = request.getRequestURI();
		String method = request.getMethod();
		return VALID_API_PATHS.getOrDefault(method, List.of()).stream()
			.anyMatch(validPath -> path.matches(validPath.replace("**", ".*")));
	}
}