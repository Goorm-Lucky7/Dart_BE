package com.dart.api.dto.review.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ReviewCreateDto(
	@NotNull(message = "[❎ ERROR] 전시회는 필수 입력 값입니다.")
	Long galleryId,
	String content,
	@Min(value = 1, message = "[❎ ERROR] 점수는 1점 이상이어야 합니다.")
	@Max(value = 5, message = "[❎ ERROR] 점수는 5점 이하여야 합니다.")
	int score
) {
}
