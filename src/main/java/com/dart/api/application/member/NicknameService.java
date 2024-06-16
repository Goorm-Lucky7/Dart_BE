package com.dart.api.application.member;

import static java.lang.Boolean.*;

import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import com.dart.api.domain.member.repository.MemberRepository;
import com.dart.api.domain.auth.repository.NicknameRedisRepository;
import com.dart.api.domain.auth.repository.SessionRedisRepository;
import com.dart.global.common.util.CookieUtil;
import com.dart.global.error.exception.ConflictException;
import com.dart.global.error.model.ErrorCode;

@Service
@RequiredArgsConstructor
public class NicknameService {

	private final MemberRepository memberRepository;
	private final NicknameRedisRepository nicknameRedisRepository;
	private final SessionRedisRepository sessionRedisRepository;

	private final CookieUtil cookieUtil;

	public void checkAndReserveNickname(String newNickname, String sessionId, HttpServletResponse response) {
		setSessionIdIfAbsent(response, sessionId);

		String oldNickname = sessionRedisRepository.findNicknameBySessionId(sessionId);
		removeOldNicknameIfChanged(oldNickname, newNickname);

		verifyNewNickname(newNickname, oldNickname);

		reserveNickname(newNickname, sessionId);
	}
	public void setNicknameVerified(String nickname) {
		if (nicknameRedisRepository.isReserved(nickname)) {
			nicknameRedisRepository.setVerified(nickname);
		}
	}

	private void setSessionIdIfAbsent(HttpServletResponse response, String sessionId) {
		if (sessionId == null || sessionId.isEmpty()) {
			cookieUtil.setSessionCookie(response);
		}
	}

	private void removeOldNicknameIfChanged(String oldNickname, String newNickname) {
		if (oldNickname != null && !oldNickname.equals(newNickname)) {
			nicknameRedisRepository.deleteNickname(oldNickname);
		}
	}

	private void verifyNewNickname(String newNickname, String oldNickname) {
		verifyNicknameUsed(newNickname);
		if (oldNickname != null && !oldNickname.equals(newNickname)) {
			verifyNicknameReserved(newNickname);
		}
	}

	private void verifyNicknameUsed(String nickname) {
		if (memberRepository.existsByNickname(nickname)) {
			throw new ConflictException(ErrorCode.FAIL_NICKNAME_CONFLICT);
		}
	}

	private void verifyNicknameReserved(String nickname) {
		if (nicknameRedisRepository.isReserved(nickname) == TRUE) {
			throw new ConflictException(ErrorCode.FAIL_NICKNAME_CONFLICT);
		}
	}

	private void reserveNickname(String nickname, String sessionId) {
		nicknameRedisRepository.setNickname(nickname);
		sessionRedisRepository.saveSessionNicknameMapping(sessionId, nickname);
	}
}
