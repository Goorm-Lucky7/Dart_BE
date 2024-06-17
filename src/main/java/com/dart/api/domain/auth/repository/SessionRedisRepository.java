package com.dart.api.domain.auth.repository;

import static com.dart.global.common.util.RedisConstant.*;

import org.springframework.stereotype.Repository;

import com.dart.api.infrastructure.redis.ValueRedisRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class SessionRedisRepository {

	private final ValueRedisRepository valueRedisRepository;

	public void saveSessionEmailMapping(String sessionId, String email) {
		valueRedisRepository.saveValueWithExpiry(REDIS_SESSION_EMAIL_PREFIX + sessionId, email, SESSION_EMAIL_EXPIRATION_TIME_SECONDS);
	}

	public void saveSessionNicknameMapping(String sessionId, String nickname) {
		valueRedisRepository.saveValueWithExpiry(REDIS_SESSION_NICKNAME_PREFIX + sessionId, nickname, SESSION_NICKNAME_EXPIRATION_TIME_SECONDS);
	}

	public String findEmailBySessionId(String sessionId) {
		return valueRedisRepository.getValue(REDIS_SESSION_EMAIL_PREFIX + sessionId);
	}

	public String findNicknameBySessionId(String sessionId) {
		return valueRedisRepository.getValue(REDIS_SESSION_NICKNAME_PREFIX + sessionId);
	}

	public void deleteSessionEmailMapping(String sessionId) {
		valueRedisRepository.deleteValue(REDIS_SESSION_EMAIL_PREFIX + sessionId);
	}

	public void deleteSessionNicknameMapping(String sessionId) {
		valueRedisRepository.deleteValue(REDIS_SESSION_NICKNAME_PREFIX + sessionId);
	}
}
