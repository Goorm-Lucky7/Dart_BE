package com.dart.api.application.auth;

import static com.dart.global.common.util.AuthConstant.*;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.dart.api.domain.auth.AuthUser;
import com.dart.api.domain.member.entity.Member;
import com.dart.api.domain.member.repo.MemberRepository;
import com.dart.global.error.exception.NotFoundException;
import com.dart.global.error.exception.UnauthorizedException;
import com.dart.global.error.model.ErrorCode;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtProviderService {

	private static final String EMAIL = "email";
	private static final String NICKNAME = "nickname";
	private static final String PROFILE_IMAGE_URL = "profileImageUrl";

	@Value("${jwt.secret.access-key}")
	private String secret;

	@Value("${jwt.access-expire}")
	private long accessTokenExpire;

	private SecretKey secretKey;

	private final MemberRepository memberRepository;

	@PostConstruct
	private void init() {
		secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
	}

	public String generateToken(String email, String nickname) {
		final Date issuedDate = new Date();
		final Date expiredDate = new Date(issuedDate.getTime() + accessTokenExpire);

		return buildJwt(issuedDate, expiredDate)
			.claim(EMAIL, email)
			.claim(NICKNAME, nickname)
			.compact();
	}

	@Transactional
	public String reGenerateToken(String accessToken) {
		final Claims claims = getClaimsByToken(accessToken);
		final String email = claims.get(EMAIL, String.class);
		final Member member = memberRepository.findByEmail(email)
			.orElseThrow(() -> new NotFoundException(ErrorCode.FAIL_MEMBER_NOT_FOUND));

		return generateToken(member.getEmail(), member.getNickname());
	}

	public String extractToken(String header, HttpServletRequest request) {
		String token = request.getHeader(header);

		if (token == null || !token.startsWith(BEARER)) {
			log.warn("====== {} is null or not bearer =======", header);
			return null;
		}

		return token.replaceFirst(BEARER, "").trim();
	}

	public AuthUser extractAuthUserByAccessToken(String token) {
		final Claims claims = getClaimsByToken(token);
		final String email = claims.get(EMAIL, String.class);
		final String nickname = claims.get(NICKNAME, String.class);

		return AuthUser.create(email, nickname);
	}

	public boolean isUsable(String token) {
		try {
			Jwts.parser()
				.verifyWith(secretKey)
				.build()
				.parseSignedClaims(token);

			return true;
		} catch (ExpiredJwtException e) {
			log.warn("====== TOKEN EXPIRED ======");
		} catch (IllegalArgumentException e) {
			log.warn("====== EMPTIED TOKEN ======");
		} catch (Exception e) {
			log.warn("====== INVALID TOKEN ======");
			throw new UnauthorizedException(ErrorCode.FAIL_INVALID_TOKEN);
		}

		return false;
	}

	private JwtBuilder buildJwt(Date issuedDate, Date expiredDate) {
		return Jwts.builder()
			.issuedAt(issuedDate)
			.expiration(expiredDate)
			.signWith(secretKey, Jwts.SIG.HS256)
			.setHeaderParam("alg", "HS256")
			.setHeaderParam("typ", "JWT");
	}

	private Claims getClaimsByToken(String token) {
		try {
			return Jwts.parser()
				.verifyWith(secretKey)
				.build()
				.parseSignedClaims(token)
				.getPayload();
		} catch (ExpiredJwtException e) {
			return e.getClaims();
		} catch (Exception e) {
			throw new UnauthorizedException(ErrorCode.FAIL_TOKEN_EXPIRED);
		}
	}
}
