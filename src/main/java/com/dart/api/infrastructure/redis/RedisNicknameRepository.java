package com.dart.api.infrastructure.redis;

import static com.dart.api.infrastructure.redis.RedisConstant.*;
import static org.apache.commons.lang3.BooleanUtils.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class RedisNicknameRepository {

	private final RedisHashRepository redisHashRepository;

	@Transactional
	public void setNickname(String nickname) {
		Map<String, String> nicknameInfo = new HashMap<>();
		nicknameInfo.put(NICKNAME_RESERVED, TRUE);
		nicknameInfo.put(NICKNAME_VERIFIED, FALSE);

		redisHashRepository.setHashOpsAndExpire(REDIS_NICKNAME_PREFIX + nickname, nicknameInfo,
			TimeUnit.MILLISECONDS.toSeconds(SESSION_NICKNAME_EXPIRATION));
	}

	public void setVerified(String nickname) {
		redisHashRepository.updateHashOpsField(REDIS_NICKNAME_PREFIX + nickname, NICKNAME_VERIFIED, TRUE);
	}

	public void deleteNickname(String nickname) {
		redisHashRepository.delete(REDIS_NICKNAME_PREFIX + nickname);
	}

	@Transactional(readOnly = true)
	public boolean isReserved(String nickname) {
		System.out.println("redis isReserved: "+redisHashRepository.getHashOps(REDIS_NICKNAME_PREFIX + nickname, NICKNAME_RESERVED));
		return TRUE.equals(redisHashRepository.getHashOps(REDIS_NICKNAME_PREFIX + nickname, NICKNAME_RESERVED));
	}

	@Transactional(readOnly = true)
	public boolean isVerified(String nickname) {
		return TRUE.equals(redisHashRepository.getHashOps(REDIS_NICKNAME_PREFIX + nickname, NICKNAME_VERIFIED));
	}
}
