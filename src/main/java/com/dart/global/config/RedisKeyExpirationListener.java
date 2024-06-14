package com.dart.global.config;

import static com.dart.api.infrastructure.redis.RedisConstant.*;

import java.util.List;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.dart.api.application.gallery.ImageService;
import com.dart.api.domain.chat.entity.ChatRoom;
import com.dart.api.domain.chat.repository.ChatRedisRepository;
import com.dart.api.domain.chat.repository.ChatRoomRepository;
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
	private final ChatRoomRepository chatRoomRepository;
	private final ChatRedisRepository chatRedisRepository;

	public RedisKeyExpirationListener(
		RedisMessageListenerContainer listenerContainer,
		GalleryRepository galleryRepository,
		ImageService imageService,
		HashtagRepository hashtagRepository,
		ChatRoomRepository chatRoomRepository,
		ChatRedisRepository chatRedisRepository
	) {
		super(listenerContainer);
		this.galleryRepository = galleryRepository;
		this.imageService = imageService;
		this.hashtagRepository = hashtagRepository;
		this.chatRoomRepository = chatRoomRepository;
		this.chatRedisRepository = chatRedisRepository;
	}

	@Override
	public void onMessage(Message message, byte[] pattern) {
		final String expiredKey = message.toString();

		if (isPaymentKey(expiredKey)) {
			final Long galleryId = Long.parseLong(expiredKey.replace(REDIS_PAYMENT_PREFIX, ""));
			handleExpiredGallery(galleryId);
		} else if (isChatMessageKey(expiredKey)) {
			final Long chatRoomId = Long.parseLong(expiredKey.replace(REDIS_CHAT_MESSAGE_PREFIX, ""));
			handleExpiredChatMessages(chatRoomId);
		}
	}

	public void handleExpiredGallery(Long galleryId) {
		final Gallery gallery = galleryRepository.findById(galleryId)
			.orElseThrow(() -> new NotFoundException(ErrorCode.FAIL_GALLERY_NOT_FOUND));
		final List<Hashtag> hashtags = hashtagRepository.findByGallery(gallery);
		final ChatRoom chatRoom = chatRoomRepository.findByGallery(gallery)
			.orElseThrow(() -> new NotFoundException(ErrorCode.FAIL_CHAT_ROOM_NOT_FOUND));

		chatRoomRepository.delete(chatRoom);
		imageService.deleteImagesByGallery(gallery);
		imageService.deleteThumbnail(gallery);
		hashtagRepository.deleteAll(hashtags);
		galleryRepository.delete(gallery);
	}

	public void handleExpiredChatMessages(Long chatRoomId) {
		chatRedisRepository.deleteChatMessages(chatRoomId);
	}

	private boolean isPaymentKey(String str) {
		return str.contains(REDIS_PAYMENT_PREFIX);
	}

	private boolean isChatMessageKey(String str) {
		return str.startsWith(REDIS_CHAT_MESSAGE_PREFIX);
	}
}
