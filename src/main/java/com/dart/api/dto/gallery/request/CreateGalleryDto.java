package com.dart.api.dto.gallery.request;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record CreateGalleryDto(
	@NotBlank(message = "[❎ ERROR] 전시 제목을 입력해주세요.")
	@Length(max = 30, message = "[❎ ERROR] 전시 제목은 30자까지 입력 가능합니다.")
	String title,
	@NotBlank(message = "[❎ ERROR] 전시 설명을 입력해주세요.")
	@Length(max = 250, message = "[❎ ERROR] 전시 설명은 250자까지 입력 가능합니다.")
	String content,
	@NotNull(message = "[❎ ERROR] 전시 시작일을 입력해주세요.")
	LocalDateTime startDate,
	LocalDateTime endDate,
	@NotBlank(message = "[❎ ERROR] 전시에 사용할 템플릿을 선택해주세요.")
	String template,
	@NotNull(message = "[❎ ERROR] 전시 입장료를 입력해주세요.(무료이면 0원을 입력해주세요.)")
	Integer fee,
	@NotNull(message = "[❎ ERROR] 전시 생성 금액을 입력해주세요.(무료이면 0원을 입력해주세요.)")
	Integer generatedCost,
	String address,
	List<String> hashtags,
	@Valid @NotNull(message = "[❎ ERROR] 전시 작품들에 대한 제목과 설명을 입력해주세요.")
	List<ImageInfoDto> informations
) {
}
