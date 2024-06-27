package com.dart.api.dto.chat.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MemberSessionDto(
	@JsonProperty("nickname") String nickname,
	@JsonProperty("sessionId") String sessionId,
	@JsonProperty("destination") String destination,
	@JsonProperty("profileImageUrl") String profileImageUrl
) {
}
