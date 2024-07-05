package com.dart.api.application.notification;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dart.api.domain.gallery.entity.Gallery;
import com.dart.api.domain.gallery.repository.GalleryRepository;
import com.dart.api.domain.notification.repository.SSESessionRepository;
import com.dart.api.dto.notification.response.NotificationReadDto;
import com.dart.global.error.exception.NotFoundException;
import com.dart.support.GalleryFixture;

@ExtendWith(MockitoExtension.class)
class ExhibitionNotificationServiceTest {

	@Mock
	private SSESessionRepository sseSessionRepository;

	@Mock
	private GalleryRepository galleryRepository;

	@InjectMocks
	private ExhibitionNotificationService exhibitionNotificationService;

	@Test
	@DisplayName("SEND RE-EXHIBITION REQUEST NOTIFICATION(⭕️ SUCCESS): 성공적으로 재전시 요청 알림을 전송했습니다.")
	void sendReExhibitionRequestNotification_void_success() {
		// GIVEN
		Long galleryId = 1L;
		Gallery gallery = GalleryFixture.createGalleryEntity();
		Long authorId = gallery.getMember().getId();

		when(galleryRepository.findById(anyLong())).thenReturn(Optional.of(gallery));
		doNothing().when(sseSessionRepository).sendEvent(eq(authorId), any(NotificationReadDto.class));

		// WHEN
		exhibitionNotificationService.sendReExhibitionRequestNotification(galleryId);

		// THEN
		verify(sseSessionRepository).sendEvent(eq(authorId), any(NotificationReadDto.class));
		verify(galleryRepository).findById(galleryId);
	}

	@Test
	@DisplayName("SEND RE-EXHIBITION REQUEST NOTIFICATION(❌ FAILURE): 전시회를 찾을 수 없어 재전시 요청 알림 전송에 실패했습니다.")
	void sendReExhibitionRequestNotification_NotFoundException_fail() {
		// GIVEN
		Long galleryId = 1L;

		when(galleryRepository.findById(galleryId)).thenReturn(Optional.empty());

		// WHEN & THEN
		assertThatThrownBy(() -> exhibitionNotificationService.sendReExhibitionRequestNotification(galleryId))
			.isInstanceOf(NotFoundException.class)
			.hasMessageContaining("[❎ ERROR] 요청하신 전시관을 찾을 수 없습니다.");

		verify(sseSessionRepository, never()).sendEvent(anyLong(), any(NotificationReadDto.class));
	}
}
