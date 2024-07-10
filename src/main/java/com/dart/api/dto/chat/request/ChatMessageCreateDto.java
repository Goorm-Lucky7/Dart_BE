package com.dart.api.dto.chat.request;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record ChatMessageCreateDto(
	@Size(max = 50, message = "[❎ ERROR] 메시지 내용은 50자 이내여야 합니다.")
	String content,

	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	LocalDateTime createdAt,
	String sender,
	String profileImageUrl,
	boolean isAuthor
) {
}
