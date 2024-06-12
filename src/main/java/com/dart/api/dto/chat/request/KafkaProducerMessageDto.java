package com.dart.api.dto.chat.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record KafkaProducerMessageDto(
	String sender,

	@NotBlank(message = "[❎ ERROR] 메시지 내용을 입력해주세요.")
	@Size(max = 50)
	String content
) {
}
