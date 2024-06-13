package com.dart.api.infrastructure.redis;

import static com.dart.api.infrastructure.redis.RedisConstant.*;

import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class RedisSessionRepository {

	private final RedisHashRepository redisHashRepository;

	public void saveSessionEmailMapping(String sessionId, String email) {
		redisHashRepository.setExpire(REDIS_SESSION_EMAIL_PREFIX + sessionId, email, SESSION_EMAIL_EXPIRATION);
	}

	public void saveSessionNicknameMapping(String sessionId, String nickname) {
		redisHashRepository.setExpire(REDIS_SESSION_NICKNAME_PREFIX + sessionId, nickname, SESSION_NICKNAME_EXPIRATION);
	}

	public String findEmailBySessionId(String sessionId) {
		return redisHashRepository.get(REDIS_SESSION_EMAIL_PREFIX + sessionId);
	}

	public String findNicknameBySessionId(String sessionId) {
		return redisHashRepository.get(REDIS_SESSION_NICKNAME_PREFIX + sessionId);
	}
}
