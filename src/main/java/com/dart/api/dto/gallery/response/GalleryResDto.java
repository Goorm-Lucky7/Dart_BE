package com.dart.api.dto.gallery.response;

import java.util.List;

public record GalleryResDto(
	String title,
	boolean hasComment,
	String nickname,
	List<ImageResDto> images
) {
}
