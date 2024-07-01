package com.dart.api.dto.notification.response;

import lombok.Builder;

@Builder
public record NotificationReadDto(
	Object message,
	String type
) {
}
