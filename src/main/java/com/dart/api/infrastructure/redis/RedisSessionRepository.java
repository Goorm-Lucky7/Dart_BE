package com.dart.api.infrastructure.redis;

import static com.dart.api.infrastructure.redis.RedisConstant.*;

import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class RedisSessionRepository {

	private final RedisHashRepository redisHashRepository;

	public void saveSessionEmailMapping(String sessionId, String email) {
		redisHashRepository.setExpire(SESSION_EMAIL_PREFIX + sessionId, email, SESSION_EMAIL_EXPIRATION_TIME_SECONDS);
	}

	public void saveSessionNicknameMapping(String sessionId, String nickname) {
		redisHashRepository.setExpire(SESSION_NICKNAME_PREFIX + sessionId, nickname, SESSION_NICKNAME_EXPIRATION_TIME_SECONDS);
	}

	public String findEmailBySessionId(String sessionId) {
		return redisHashRepository.get(SESSION_EMAIL_PREFIX + sessionId);
	}

	public String findNicknameBySessionId(String sessionId) {
		return redisHashRepository.get(SESSION_NICKNAME_PREFIX + sessionId);
	}

	public void deleteSessionEmailMapping(String sessionId) {
		redisHashRepository.delete(SESSION_EMAIL_PREFIX + sessionId);
	}

	public void deleteSessionNicknameMapping(String sessionId) {
		redisHashRepository.delete(SESSION_NICKNAME_PREFIX + sessionId);
	}
}
