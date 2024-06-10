package com.dart.global.config;

import java.util.List;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.dart.api.application.gallery.ImageService;
import com.dart.api.domain.gallery.entity.Gallery;
import com.dart.api.domain.gallery.entity.Hashtag;
import com.dart.api.domain.gallery.repository.GalleryRepository;
import com.dart.api.domain.gallery.repository.HashtagRepository;
import com.dart.global.error.exception.NotFoundException;
import com.dart.global.error.model.ErrorCode;

@Component
@Transactional
public class RedisKeyExpirationListener extends KeyExpirationEventMessageListener {
	private final GalleryRepository galleryRepository;
	private final ImageService imageService;
	private final HashtagRepository hashtagRepository;

	public RedisKeyExpirationListener(
		RedisMessageListenerContainer listenerContainer,
		GalleryRepository galleryRepository,
		ImageService imageService,
		HashtagRepository hashtagRepository
	) {
		super(listenerContainer);
		this.galleryRepository = galleryRepository;
		this.imageService = imageService;
		this.hashtagRepository = hashtagRepository;
	}

	@Override
	public void onMessage(Message message, byte[] pattern) {
		final String expiredKey = message.toString();

		if (isLongParsable(expiredKey)) {
			final Gallery gallery = galleryRepository.findById(Long.parseLong(expiredKey))
				.orElseThrow(() -> new NotFoundException(ErrorCode.FAIL_GALLERY_NOT_FOUND));
			final List<Hashtag> hashtags = hashtagRepository.findByGallery(gallery);

			imageService.deleteImagesByGallery(gallery);
			imageService.deleteThumbnail(gallery);
			hashtagRepository.deleteAll(hashtags);
			galleryRepository.delete(gallery);
		}
	}

	public boolean isLongParsable(String str) {
		return str != null && str.matches("-?\\d+");
	}
}
