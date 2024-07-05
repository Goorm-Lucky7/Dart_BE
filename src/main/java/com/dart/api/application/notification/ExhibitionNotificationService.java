package com.dart.api.application.notification;

import static com.dart.global.common.util.SSEConstant.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dart.api.domain.gallery.entity.Gallery;
import com.dart.api.domain.gallery.repository.GalleryRepository;
import com.dart.api.domain.notification.entity.Notification;
import com.dart.api.domain.notification.entity.NotificationType;
import com.dart.api.domain.notification.repository.SSESessionRepository;
import com.dart.api.dto.notification.response.NotificationReadDto;
import com.dart.global.error.exception.NotFoundException;
import com.dart.global.error.model.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExhibitionNotificationService {

	private final SSESessionRepository sseSessionRepository;
	private final GalleryRepository galleryRepository;

	@Transactional
	public void sendReExhibitionRequestNotification(Long galleryId) {
		final Long authorId = getAuthorIdByGalleryId(galleryId);
		notificationAuthorAboutReExhibitionRequest(authorId);
	}

	private void notificationAuthorAboutReExhibitionRequest(Long memberId) {
		final NotificationReadDto notificationReadDto = Notification.createNotificationReadDto(
			REEXHIBITION_REQUEST_MESSAGE, NotificationType.REEXHIBITION.getName());

		sseSessionRepository.sendEvent(memberId, notificationReadDto);
	}

	private Long getAuthorIdByGalleryId(Long galleryId) {
		return galleryRepository.findById(galleryId)
			.orElseThrow(() -> new NotFoundException(ErrorCode.FAIL_GALLERY_NOT_FOUND))
			.getMember().getId();
	}
}
