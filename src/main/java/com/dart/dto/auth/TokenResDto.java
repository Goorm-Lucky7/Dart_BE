package com.dart.dto.auth;

import lombok.Builder;

@Builder
public record TokenResDto(
	String accessToken
) {
}
