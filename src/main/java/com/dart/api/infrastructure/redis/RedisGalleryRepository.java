package com.dart.api.infrastructure.redis;

import static com.dart.api.infrastructure.redis.RedisConstant.REDIS_GALLERY_PREFIX;

import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class RedisGalleryRepository {

	private final RedisHashRepository redisHashRepository;

	public void setGallery(String key, String data, long duration) {
		redisHashRepository.setExpire(REDIS_GALLERY_PREFIX + key, data, duration);
	}

}
