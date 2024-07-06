package com.dart.api.dto.notification.response;

import lombok.Builder;

@Builder
public record NotificationReadDto(
	String eventId,
	Object message,
	String type
) {
}
