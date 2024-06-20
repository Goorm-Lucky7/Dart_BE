package com.dart.api.dto.gallery.request;

import org.hibernate.validator.constraints.Length;

public record ImageInfoDto(
	@Length(max = 250, message = "[❎ ERROR] 작품 제목은 250자까지 입력 가능합니다.")
	String imageTitle,
	@Length(max = 250, message = "[❎ ERROR] 작품 설명은 250자까지 입력 가능합니다.")
	String description
) {
}
