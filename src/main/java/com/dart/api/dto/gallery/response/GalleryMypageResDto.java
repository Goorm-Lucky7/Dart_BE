package com.dart.api.dto.gallery.response;

import java.time.LocalDateTime;
import java.util.List;

public record GalleryMypageResDto(
	Long galleryId,
	String thumbnail,
	String title,
	LocalDateTime startDate,
	LocalDateTime endDate,
	Integer fee,
	List<String> hashtags
) {
}
