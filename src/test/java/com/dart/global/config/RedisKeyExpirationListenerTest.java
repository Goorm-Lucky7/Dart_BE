package com.dart.global.config;

import static com.dart.global.common.util.RedisConstant.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.redis.connection.DefaultMessage;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.dart.api.application.gallery.ImageService;
import com.dart.api.domain.chat.entity.ChatRoom;
import com.dart.api.domain.chat.repository.ChatRoomRepository;
import com.dart.api.domain.gallery.entity.Gallery;
import com.dart.api.domain.gallery.repository.GalleryRepository;
import com.dart.api.domain.gallery.repository.HashtagRepository;
import com.dart.api.infrastructure.redis.RedisKeyExpirationListener;
import com.dart.global.error.exception.NotFoundException;
import com.dart.support.ChatFixture;
import com.dart.support.GalleryFixture;

@ExtendWith(SpringExtension.class)
class RedisKeyExpirationListenerTest {

	@Mock
	private GalleryRepository galleryRepository;

	@Mock
	private ImageService imageService;

	@Mock
	private HashtagRepository hashtagRepository;

	@Mock
	private ChatRoomRepository chatRoomRepository;

	@InjectMocks
	private RedisKeyExpirationListener redisKeyExpirationListener;

	@Mock
	private RedisMessageListenerContainer redisMessageListenerContainer;

	@BeforeEach
	void setUp() {
		redisKeyExpirationListener = new RedisKeyExpirationListener(
			redisMessageListenerContainer,
			galleryRepository,
			imageService,
			hashtagRepository,
			chatRoomRepository
		);
	}

	@Test
	@DisplayName("HANDLE EXPIRED GALLERY(⭕️ SUCCESS): 성공적으로 만료된 전시회 KEY값을 처리했습니다.")
	void handleExpiredGallery_void_success() {
		// GIVEN
		Long galleryId = 1L;
		String expiredKey = REDIS_PAYMENT_PREFIX + galleryId;
		byte[] body = expiredKey.getBytes(StandardCharsets.UTF_8);
		Message message = new DefaultMessage(body, body);

		Gallery gallery = GalleryFixture.createGalleryEntity();
		ChatRoom chatRoom = ChatFixture.createChatRoomEntity();

		when(galleryRepository.findById(galleryId)).thenReturn(Optional.of(gallery));
		when(chatRoomRepository.findByGallery(gallery)).thenReturn(Optional.of(chatRoom));

		// WHEN
		redisKeyExpirationListener.onMessage(message, null);

		// THEN
		verify(chatRoomRepository).delete(chatRoom);
		verify(imageService).deleteImagesByGallery(gallery);
		verify(imageService).deleteThumbnail(gallery);
		verify(hashtagRepository).deleteAll(any());
		verify(galleryRepository).delete(gallery);
	}

	@Test
	@DisplayName("HANDLE EXPIRED GALLERY(❌ FAILURE): 전시회 KEY값이 만료되어 전시회를 찾을 수 없습니다.")
	void handleExpiredGallery_gallery_NotFoundException_fail() {
		// GIVEN
		Long galleryId = 1L;

		when(galleryRepository.findById(galleryId)).thenReturn(Optional.empty());

		// WHEN & THEN
		assertThatThrownBy(() -> redisKeyExpirationListener.handleExpiredGallery(galleryId))
			.isInstanceOf(NotFoundException.class)
			.hasMessage("[❎ ERROR] 요청하신 전시관을 찾을 수 없습니다.");
	}

	@Test
	@DisplayName("HANDLE EXPIRED GALLERY(❌ FAILURE): 전시회 KEY값이 만료되어 채팅방을 찾을 수 없습니다.")
	void handleExpiredGallery_chatRoom_NotFoundException_fail() {
		// GIVEN
		Long galleryId = 1L;

		Gallery gallery = GalleryFixture.createGalleryEntity();
		when(galleryRepository.findById(galleryId)).thenReturn(Optional.of(gallery));
		when(chatRoomRepository.findByGallery(gallery)).thenReturn(Optional.empty());

		// WHEN & THEN
		assertThatThrownBy(() -> redisKeyExpirationListener.handleExpiredGallery(galleryId))
			.isInstanceOf(NotFoundException.class)
			.hasMessage("[❎ ERROR] 요청하신 채팅방을 찾을 수 없습니다.");
	}
}
