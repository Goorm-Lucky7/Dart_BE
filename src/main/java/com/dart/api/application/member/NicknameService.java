package com.dart.api.application.member;

import static com.dart.global.common.util.AuthConstant.*;
import static com.dart.global.common.util.GlobalConstant.*;
import static java.lang.Boolean.*;

import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import com.dart.api.domain.member.repository.MemberRepository;
import com.dart.api.infrastructure.redis.RedisNicknameRepository;
import com.dart.api.infrastructure.redis.RedisSessionRepository;
import com.dart.global.error.exception.ConflictException;
import com.dart.global.error.model.ErrorCode;

@Service
@RequiredArgsConstructor
public class NicknameService {

	private final MemberRepository memberRepository;
	private final RedisNicknameRepository redisNicknameRepository;
	private final RedisSessionRepository redisSessionRepository;

	public void checkAndReserveNickname(String newNickname, String sessionId, HttpServletResponse response) {
		if (sessionId == null || sessionId.isEmpty()) {
			setCookie(response);
		}

		String oldNickname = redisSessionRepository.findNicknameBySessionId(sessionId);
		if (oldNickname != null && !oldNickname.equals(newNickname)) {
			redisNicknameRepository.deleteNickname(oldNickname);
		}

		verifyNicknameUsed(newNickname);
		if(oldNickname != null && !oldNickname.equals(newNickname)) {
			verifyNicknameReserved(newNickname);
		}
		reserveNickname(newNickname, sessionId);
	}

	private void setCookie(HttpServletResponse response){
		String sessionId = UUID.randomUUID().toString();
		ResponseCookie cookie = ResponseCookie.from(SESSION_ID, sessionId)
			.httpOnly(true)
			.secure(true)
			.path("/")
			.maxAge(THIRTY_DAYS)
			.sameSite("None")
			.build();

		response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
	}

	public void setNicknameVerified(String nickname) {
		if(redisNicknameRepository.isReserved(nickname)){
			redisNicknameRepository.setVerified(nickname);
		}
	}

	private void verifyNicknameUsed(String nickname) {
		if (memberRepository.existsByNickname(nickname)) {
			throw new ConflictException(ErrorCode.FAIL_NICKNAME_CONFLICT);

		}
	}

	private void verifyNicknameReserved(String nickname) {
		if (redisNicknameRepository.isReserved(nickname) == TRUE) {
			throw new ConflictException(ErrorCode.FAIL_NICKNAME_CONFLICT);
		}
	}

	private void reserveNickname(String nickname, String sessionId) {
		redisNicknameRepository.setNickname(nickname);
		redisSessionRepository.saveSessionNicknameMapping(sessionId, nickname);
	}
}
