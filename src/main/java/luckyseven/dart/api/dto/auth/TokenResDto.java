package luckyseven.dart.api.dto.auth;

import lombok.Builder;

@Builder
public record TokenResDto(
	String accessToken
) {
}