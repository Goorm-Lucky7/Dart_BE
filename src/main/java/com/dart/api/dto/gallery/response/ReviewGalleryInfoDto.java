package com.dart.api.dto.gallery.response;

import java.time.LocalDateTime;

public record ReviewGalleryInfoDto(
	String thumbnail,
	String nickname,
	String profileImage,
	String title,
	LocalDateTime startDate,
	LocalDateTime endDate,
	float reviewAverage
) {
}
