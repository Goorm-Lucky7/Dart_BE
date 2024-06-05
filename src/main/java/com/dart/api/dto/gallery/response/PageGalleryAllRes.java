package com.dart.api.dto.gallery.response;

import java.util.List;

public record PageGalleryAllRes(
	List<GalleryAllResDto> galleries,
	PageInfo pageInfo
) {
}
