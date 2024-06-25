package com.dart.api.dto.chat.response;

import java.time.LocalDateTime;

import lombok.Builder;

@Builder
public record ChatMessageReadDto(
	String sender,
	String content,
	LocalDateTime createdAt,
	boolean isAuthor,
	String profileImageUrl
) {
}
