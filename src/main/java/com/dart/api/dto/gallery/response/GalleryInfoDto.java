package com.dart.api.dto.gallery.response;

import java.time.LocalDateTime;
import java.util.List;

public record GalleryInfoDto(
	String thumbnail,
	String nickname,
	String profileImage,
	String title,
	String content,
	LocalDateTime startDate,
	LocalDateTime endDate,
	Integer fee,
	float reviewAverage,
	boolean hasTicket,
	boolean isOpen,
	List<String> hashtags
) {
}
