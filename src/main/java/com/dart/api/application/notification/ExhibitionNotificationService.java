package com.dart.api.application.notification;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dart.api.domain.gallery.entity.Gallery;
import com.dart.api.domain.gallery.repository.GalleryRepository;
import com.dart.api.domain.notification.entity.Notification;
import com.dart.api.domain.notification.entity.NotificationType;
import com.dart.api.domain.notification.repository.NotificationRepository;
import com.dart.api.domain.notification.repository.SSESessionRepository;
import com.dart.global.error.exception.NotFoundException;
import com.dart.global.error.model.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExhibitionNotificationService {

	private final NotificationRepository notificationRepository;
	private final SSESessionRepository sseSessionRepository;
	private final GalleryRepository galleryRepository;

	@Transactional
	public void sendReExhibitionRequestNotification(Long galleryId) {
		final Gallery gallery = getGalleryById(galleryId);
		final String message =
			"EXHIBITION '" + gallery.getTitle() + "' HAS RECEIVED MORE THAN 10 RE-EXHIBITION REQUESTS. "
				+ "PLEASE CONSIDER RE-EXHIBITION";

		sendReExhibitionRequestEventNotification(gallery, message);
	}

	private Gallery getGalleryById(Long galleryId) {
		return galleryRepository.findById(galleryId)
			.orElseThrow(() -> new NotFoundException(ErrorCode.FAIL_GALLERY_NOT_FOUND));
	}

	private void sendReExhibitionRequestEventNotification(Gallery gallery, String exhibitionDetail) {
		sseSessionRepository.sendEvent(
			gallery.getMember().getId(),
			exhibitionDetail,
			NotificationType.REEXHIBITION_REQUEST.getName()
		);
		log.info("[✅ LOGGER] REEXHIBITION REQUEST NOTIFICATION SENT TO AUTHOR ID: {}", gallery.getMember().getId());

		saveCommonNotification(exhibitionDetail, gallery);
	}

	private void saveCommonNotification(String message, Gallery gallery) {
		if (notificationRepository.existsByNotificationType(NotificationType.REEXHIBITION_REQUEST)) {
			log.info("[✅ LOGGER] DUPLICATE REEXHIBITION REQUEST NOTIFICATION DETECTED: {}", message);
			return;
		}

		final Notification notification = Notification.createNotification(
			message,
			NotificationType.REEXHIBITION_REQUEST,
			"https://dartgallery.site/api/galleries/" + gallery.getId()
		);
		notificationRepository.save(notification);
	}
}
