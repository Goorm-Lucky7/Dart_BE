package com.dart.api.dto.gallery.response;

import java.util.List;

public record GalleryResDto(
	String title,
	boolean hasComment,
	String nickname,
	String template,
	List<ImageResDto> images,
	Long chatRoomId,
	boolean hasTicket
) {
}
