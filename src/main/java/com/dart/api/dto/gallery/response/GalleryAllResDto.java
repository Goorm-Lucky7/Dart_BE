package com.dart.api.dto.gallery.response;

import java.time.LocalDateTime;

public record GalleryAllResDto(
	Long galleryId,
	String thumbnail,
	String title,
	LocalDateTime startDate,
	LocalDateTime endDate
) {
}
