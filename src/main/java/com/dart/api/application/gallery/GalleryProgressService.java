package com.dart.api.application.gallery;

import static com.dart.global.common.util.GlobalConstant.*;
import static com.dart.global.common.util.RedisConstant.*;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.dart.api.domain.notification.repository.SSESessionRepository;
import com.dart.api.infrastructure.redis.ValueRedisRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GalleryProgressService {

	private final ValueRedisRepository valueRedisRepository;
	private final SSESessionRepository sseSessionRepository;

	public void addEmitter(Long galleryId, SseEmitter emitter) {
		sseSessionRepository.addSSEEmitter(galleryId, emitter);
	}

	public void updateProgress(Long galleryId, int progress) {
		sseSessionRepository.sendEvent(galleryId, progress, "Progress Update");

		if (progress == ONE_HUNDRED_PERCENT) {
			sseSessionRepository.deleteSSEEmitterByClientId(galleryId);
			deleteProgressFromRedis(galleryId);
		} else {
			saveProgressToRedis(galleryId, progress);
		}
	}

	public void saveProgressToRedis(Long galleryId, int progress) {
		String key = REDIS_GALLERY_PREFIX + galleryId;
		valueRedisRepository.saveValue(key, String.valueOf(progress));
	}

	public void deleteProgressFromRedis(Long galleryId) {
		String key = REDIS_GALLERY_PREFIX + galleryId;
		valueRedisRepository.deleteValue(key);
	}
}
