package com.dart.api.infrastructure.redis;

import static com.dart.api.infrastructure.redis.RedisConstant.*;
import static com.dart.global.common.util.GlobalConstant.*;
import static org.apache.commons.lang3.BooleanUtils.*;

import java.util.HashMap;
import java.util.Map;

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
		nicknameInfo.put(RESERVED, TRUE);
		nicknameInfo.put(VERIFIED, FALSE);

		redisHashRepository.setHashOpsAndExpire(NICKNAME_PREFIX + nickname, nicknameInfo,
			NICKNAME_VERIFICATION_EXPIRATION_TIME_SECONDS);
	}

	public void setVerified(String nickname) {
		redisHashRepository.updateHashOpsField(NICKNAME_PREFIX + nickname, VERIFIED, TRUE);
	}

	public void deleteNickname(String nickname) {
		redisHashRepository.delete(NICKNAME_PREFIX + nickname);
	}

	@Transactional(readOnly = true)
	public boolean isReserved(String nickname) {
		return TRUE.equals(redisHashRepository.getHashOps(NICKNAME_PREFIX + nickname, RESERVED));
	}

	@Transactional(readOnly = true)
	public boolean isVerified(String nickname) {
		return TRUE.equals(redisHashRepository.getHashOps(NICKNAME_PREFIX + nickname, VERIFIED));
	}
}
