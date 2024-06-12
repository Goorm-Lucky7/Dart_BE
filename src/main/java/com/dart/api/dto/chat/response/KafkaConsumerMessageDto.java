package com.dart.api.dto.chat.response;

import lombok.Builder;

@Builder
public record KafkaConsumerMessageDto(
	String sender,
	String content
) {
}
