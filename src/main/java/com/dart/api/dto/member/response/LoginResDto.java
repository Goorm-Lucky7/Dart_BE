package com.dart.api.dto.member.response;

public record LoginResDto(
	String accessToken,
	String email,
	String nickname,
	String profileImage
) {
}
