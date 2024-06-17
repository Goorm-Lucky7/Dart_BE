package com.dart.api.domain.auth.repository;

import static com.dart.global.common.util.RedisConstant.*;
import static org.apache.commons.lang3.BooleanUtils.*;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.dart.api.infrastructure.redis.HashRedisRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class NicknameRedisRepository {

	private final HashRedisRepository hashRedisRepository;

	@Transactional
	public void setNickname(String nickname) {
		Map<String, String> nicknameInfo = new HashMap<>();
		nicknameInfo.put(RESERVED, TRUE);
		nicknameInfo.put(VERIFIED, FALSE);

		hashRedisRepository.saveHashEntriesWithExpiry(REDIS_NICKNAME_PREFIX + nickname, nicknameInfo,
			NICKNAME_VERIFICATION_EXPIRATION_TIME_SECONDS);
	}

	public void setVerified(String nickname) {
		hashRedisRepository.updateHashEntry(REDIS_NICKNAME_PREFIX + nickname, VERIFIED, TRUE);
	}

	public void deleteNickname(String nickname) {
		hashRedisRepository.deleteAllHashEntries(REDIS_NICKNAME_PREFIX + nickname);
	}

	@Transactional(readOnly = true)
	public boolean isReserved(String nickname) {
		return TRUE.equals(hashRedisRepository.getHashEntry(REDIS_NICKNAME_PREFIX + nickname, RESERVED));
	}

	@Transactional(readOnly = true)
	public boolean isVerified(String nickname) {
		return TRUE.equals(hashRedisRepository.getHashEntry(REDIS_NICKNAME_PREFIX + nickname, VERIFIED));
	}
}
