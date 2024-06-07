package com.dart.api.dto.gallery.request;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record CreateGalleryDto(
	@NotBlank(message = "[❎ ERROR] 전시 제목을 입력해주세요.")
	String title,
	@NotBlank(message = "[❎ ERROR] 전시 설명을 입력해주세요.")
	String content,
	@NotNull(message = "[❎ ERROR] 전시 시작일을 입력해주세요.")
	LocalDateTime startDate,
	LocalDateTime endDate,
	@NotNull(message = "[❎ ERROR] 전시 입장료를 입력해주세요.(무료이면 0원을 입력해주세요.)")
	Integer fee,
	List<String> hashTags,
	@NotNull(message = "[❎ ERROR] 전시 작품들을 입력해주세요.")
	List<ImageInfoDto> images
) {
}
