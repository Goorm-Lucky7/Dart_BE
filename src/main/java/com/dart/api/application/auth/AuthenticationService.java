package com.dart.api.application.auth;


import static com.dart.global.common.util.AuthConstant.*;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.dart.api.application.RedisService;
import com.dart.api.domain.auth.entity.AuthUser;
import com.dart.api.domain.member.entity.Member;
import com.dart.api.domain.member.repository.MemberRepository;
import com.dart.api.dto.member.request.LoginReqDto;
import com.dart.global.error.exception.BadRequestException;
import com.dart.global.error.exception.NotFoundException;
import com.dart.global.error.exception.UnauthorizedException;
import com.dart.global.error.model.ErrorCode;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtProviderService jwtProviderService;
	private final RedisService redisService;

	@Value("${jwt.refresh-expire}")
	private long refreshTokenExpire;

	@Transactional
	public void login(LoginReqDto loginReqDto, HttpServletResponse response) {
		final Member member = findByMemberEmail(loginReqDto.email());
		validatePasswordMatch(loginReqDto.password(), member.getPassword());

		final String accessToken = jwtProviderService.generateAccessToken(member.getEmail(), member.getNickname(), member.getProfileImageUrl());
		final String refreshToken = jwtProviderService.generateRefreshToken(member.getEmail());

		redisService.setValues(loginReqDto.email(), refreshToken, Duration.ofDays(7));

		setTokensInResponse(response, accessToken, refreshToken);
	}

	public void reissue(HttpServletRequest request, HttpServletResponse response) {
		String accessToken = request.getHeader(ACCESS_TOKEN_HEADER).replace("Bearer ", "");
		String refreshToken = getCookieValue(request, "Refresh-Token");

		validateRefreshToken(refreshToken);

		final AuthUser authUser = jwtProviderService.extractAuthUserByAccessToken(accessToken);
		validateSavedRefreshToken(authUser.email(), refreshToken);

		Member member = getMemberByEmail(authUser.email());

		String newAccessToken = jwtProviderService.generateAccessToken(member.getEmail(), member.getNickname(), member.getProfileImageUrl());
		String newRefreshToken = jwtProviderService.generateRefreshToken(authUser.email());

		redisService.deleteValues(authUser.email());
		redisService.setValues(authUser.email(), newRefreshToken, Duration.ofMillis(refreshTokenExpire));

		setTokensInResponse(response, newAccessToken, newRefreshToken);
	}

	private Member findByMemberEmail(String email) {
		return memberRepository.findByEmail(email)
			.orElseThrow(() -> new NotFoundException(ErrorCode.FAIL_MEMBER_NOT_FOUND));
	}

	private void validatePasswordMatch(String password, String encodedPassword) {
		if (!passwordEncoder.matches(password, encodedPassword)) {
			throw new BadRequestException(ErrorCode.FAIL_INCORRECT_PASSWORD);
		}
	}

	private void validateRefreshToken(String refreshToken) {
		if (!jwtProviderService.isUsable(refreshToken)) {
			throw new UnauthorizedException(ErrorCode.FAIL_INVALID_TOKEN);
		}
	}

	private void validateSavedRefreshToken(String email, String refreshToken) {
		String savedRefreshToken = redisService.getValues(email);
		if (!savedRefreshToken.equals(refreshToken)) {
			throw new UnauthorizedException(ErrorCode.FAIL_INVALID_TOKEN);
		}
	}

	private Member getMemberByEmail(String email) {
		return memberRepository.findByEmail(email)
			.orElseThrow(() -> new NotFoundException(ErrorCode.FAIL_MEMBER_NOT_FOUND));
	}

	private void setTokensInResponse(HttpServletResponse response, String accessToken, String refreshToken) {
		response.setHeader(ACCESS_TOKEN_HEADER, accessToken);

		ResponseCookie refreshTokenCookie = ResponseCookie.from("Refresh-Token", refreshToken)
			.httpOnly(true)
			.secure(true)
			.path("/")
			.maxAge(Duration.ofMillis(refreshTokenExpire).toSeconds())
			.build();

		response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
	}

	private String getCookieValue(HttpServletRequest request, String name) {
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
