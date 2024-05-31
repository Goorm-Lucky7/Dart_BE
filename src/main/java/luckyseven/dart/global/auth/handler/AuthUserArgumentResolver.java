package luckyseven.dart.global.auth.handler;

import java.util.Objects;

import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import luckyseven.dart.api.domain.auth.AuthUser;
import luckyseven.dart.global.auth.annotation.Auth;
import luckyseven.dart.global.error.exception.UnauthorizedException;

@Slf4j
public class AuthUserArgumentResolver implements HandlerMethodArgumentResolver {

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return Objects.nonNull(parameter.getParameterAnnotation(Auth.class))
			&& parameter.getParameterType().equals(AuthUser.class);
	}

	@Override
	public Object resolveArgument(@Nullable MethodParameter parameter, ModelAndViewContainer mavContainer,
		@Nullable NativeWebRequest webRequest, WebDataBinderFactory binderFactory
	) {
		Auth auth = Objects.requireNonNull(parameter).getParameterAnnotation(Auth.class);

		return getAuthUser(Objects.requireNonNull(auth).required());
	}

	private AuthUser getAuthUser(boolean isAuthRequired) throws UnauthorizedException {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken)) {
			Object principal = authentication.getPrincipal();
			if (principal instanceof AuthUser) return (AuthUser)principal;
		}

		if (isAuthRequired) {
			log.info("Unauthenticated User Access.");
		}

		return null;
	}
}
