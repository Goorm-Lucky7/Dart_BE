package com.dart.api.infrastructure.redis;

import static com.dart.global.common.util.RedisConstant.*;

import java.util.Arrays;
import java.util.List;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.dart.api.domain.gallery.entity.Gallery;
import com.dart.api.domain.gallery.repository.GalleryRepository;
import com.dart.api.domain.gallery.repository.HashtagRepository;
import com.dart.api.domain.gallery.repository.TrieRedisRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RedisDataInitializer {
	private static final List<String> REDIS_PATTERNS = Arrays.asList(
		REDIS_TITLE_PREFIX, REDIS_AUTHOR_PREFIX, REDIS_HASHTAG_PREFIX);

	private final GalleryRepository galleryRepository;
	private final HashtagRepository hashtagRepository;
	private final TrieRedisRepository trieRedisRepository;
	private final RedisKeyPatternDeleter redisKeyPatternDeleter;

	@EventListener(ApplicationReadyEvent.class)
	@Transactional
	public void initializeRedisData() {
		clearExistingRedisData();
		loadGalleryDataIntoRedis();
	}

	private void clearExistingRedisData() {
		redisKeyPatternDeleter.deleteKeysByPatterns(REDIS_PATTERNS);
	}

	private void loadGalleryDataIntoRedis() {
		List<Gallery> galleries = galleryRepository.findAll();
		for (Gallery gallery : galleries) {
			insertGalleryData(gallery);
		}
	}

	private void insertGalleryData(Gallery gallery) {
		trieRedisRepository.insert(AUTHOR, gallery.getMember().getNickname());
		trieRedisRepository.insert(TITLE, gallery.getTitle());
		insertHashtags(gallery);
	}

	private void insertHashtags(Gallery gallery) {
		hashtagRepository.findByGallery(gallery).forEach(hashtag ->
			trieRedisRepository.insert(HASHTAG, hashtag.getTag())
		);
	}
}
