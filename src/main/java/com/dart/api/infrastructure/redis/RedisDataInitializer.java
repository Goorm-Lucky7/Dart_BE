package com.dart.api.infrastructure.redis;

import static com.dart.global.common.util.RedisConstant.*;

import java.util.ArrayList;
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

	private final GalleryRepository galleryRepository;
	private final HashtagRepository hashtagRepository;
	private final TrieRedisRepository trieRedisRepository;
	private final RedisDataDeleter redisDataDeleter;

	@EventListener(ApplicationReadyEvent.class)
	@Transactional
	public void loadDataIntoRedis() {
		clearRedisData();
		List<Gallery> galleries = galleryRepository.findAll();

		for (Gallery gallery : galleries) {
			trieRedisRepository.insert(AUTHOR, gallery.getMember().getNickname());
			trieRedisRepository.insert(TITLE, gallery.getTitle());
			hashtagRepository.findByGallery(gallery).forEach(hashtag -> {
				trieRedisRepository.insert(HASHTAG, hashtag.getTag());
			});
		}
	}
	private void clearRedisData() {
		List<String> patterns = Arrays.asList(TITLE, AUTHOR, HASHTAG, REDIS_TOKEN_PREFIX);
		redisDataDeleter.deleteKeysByPattern(patterns);
	}
}
