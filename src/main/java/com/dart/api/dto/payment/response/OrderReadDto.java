package com.dart.api.dto.payment.response;

import lombok.Builder;

@Builder
public record OrderReadDto(
	String title,
	String thumbnail,
	String nickname,
	String profileImage,
	int cost
) {
}
