package com.dart.api.application.auth;

import static com.dart.global.common.util.AuthConstant.*;
import static com.dart.global.common.util.GlobalConstant.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dart.api.domain.auth.entity.RefreshToken;
import com.dart.api.domain.auth.repository.RefreshTokenRepository;
import com.dart.api.domain.auth.repository.TokenRedisRepository;
import com.dart.api.domain.member.entity.Member;
import com.dart.api.domain.member.repository.MemberRepository;
import com.dart.api.dto.auth.response.TokenResDto;
import com.dart.api.dto.member.request.LoginReqDto;
import com.dart.api.dto.member.response.LoginResDto;
import com.dart.global.common.util.CookieUtil;
import com.dart.global.error.exception.BadRequestException;
import com.dart.global.error.exception.NotFoundException;
import com.dart.global.error.exception.UnauthorizedException;
import com.dart.global.error.model.ErrorCode;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtProviderService jwtProviderService;
	private final TokenRedisRepository tokenRedisRepository;
	private final RefreshTokenRepository refreshTokenRepository;

	private final CookieUtil cookieUtil;

	@Value("${jwt.access-expire}")
	private long accessTokenExpire;

	@Value("${jwt.refresh-expire}")
	private long refreshTokenExpire;

	@Transactional
	public LoginResDto login(LoginReqDto loginReqDto, HttpServletRequest request, HttpServletResponse response) {
		final Member member = findByMemberEmail(loginReqDto.email());
		final String email = member.getEmail();
		validatePasswordMatch(loginReqDto.password(), member.getPassword());

		String clientInfo = extractClientInfo(request);

		if (refreshTokenRepository.existsByEmail(email)) {
			refreshTokenRepository.deleteByEmail(email);
		}

		final String accessToken = jwtProviderService.generateAccessToken(member.getId(), member.getEmail(),
			member.getNickname(), member.getProfileImageUrl(), clientInfo);

		final String refreshToken = jwtProviderService.generateRefreshToken(member.getEmail());

		saveTokensInResponse(response, accessToken, refreshToken);

		return new LoginResDto(accessToken, member.getEmail(), member.getNickname(), member.getProfileImageUrl());
	}

	@Transactional
	public TokenResDto reissue(HttpServletRequest request, HttpServletResponse response) {
		String accessToken = extractTokenFromHeader(request);
		String refreshToken = cookieUtil.getCookie(request, REFRESH_TOKEN_COOKIE_NAME);
		String clientInfo = extractClientInfo(request);

		if ( accessToken == null || refreshToken == null) {
			throw new NotFoundException(ErrorCode.FAIL_TOKEN_NOT_FOUND);
		}

		try {
			String email = jwtProviderService.extractEmailFromToken(refreshToken);

			if (jwtProviderService.isValidRefreshToken(refreshToken)) {
				RefreshToken refreshTokenEntity = refreshTokenRepository.findByToken(refreshToken)
					.orElseThrow(() -> new UnauthorizedException(ErrorCode.FAIL_TOKEN_NOT_FOUND));

				if (refreshTokenEntity.isExpired()) {
					throw new UnauthorizedException(ErrorCode.FAIL_TOKEN_EXPIRED);
				}

				if (!refreshToken.equals(refreshTokenEntity.getToken())) {
					throw new UnauthorizedException(ErrorCode.FAIL_INVALID_TOKEN);
				}

				handleRefreshToken(email);

				String newAccessToken = jwtProviderService.generateAccessToken(
					jwtProviderService.extractIdFromToken(accessToken),
					email,
					jwtProviderService.extractNicknameFromToken(accessToken),
					jwtProviderService.extractProfileImageFromToken(accessToken),
					clientInfo
				);

				String newRefreshToken = jwtProviderService.generateRefreshToken(email);

				saveTokensInResponse(response, newAccessToken, newRefreshToken);
				return new TokenResDto(newAccessToken);
			} else {
				throw new UnauthorizedException(ErrorCode.FAIL_INVALID_TOKEN);
			}
		} catch (Exception e) {
			throw new UnauthorizedException(ErrorCode.FAIL_INVALID_TOKEN);
		}
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

	private String extractTokenFromHeader(HttpServletRequest request) {
		return request.getHeader(ACCESS_TOKEN_HEADER).replace(BEARER, BLANK).trim();
	}

	private void saveTokensInResponse(HttpServletResponse response, String accessToken, String refreshToken) {
		saveAccessTokenInResponse(response, accessToken);
		saveRefreshTokenInResponse(response, refreshToken);
	}

	private void saveAccessTokenInResponse(HttpServletResponse response, String accessToken) {
		response.setHeader(ACCESS_TOKEN_HEADER, accessToken);
	}

	private void saveRefreshTokenInResponse(HttpServletResponse response, String refreshToken) {
		cookieUtil.setRefreshCookie(response, refreshToken);
	}

	private String extractClientInfo(HttpServletRequest request) {
		String ipAddress = request.getRemoteAddr();
		String userAgent = request.getHeader("User-Agent");
		return ipAddress + "|" + userAgent;
	}

	@Transactional
	public void handleRefreshToken(String email) {
		try {
			if (refreshTokenRepository.existsByEmail(email)) {
				refreshTokenRepository.deleteByEmail(email);
			} else {
			}
		} catch (Exception e) {
			throw e;
		}
	}
}
