package com.dart.api.dto.notification.response;

import com.dart.api.domain.notification.entity.NotificationType;

import lombok.Builder;

@Builder
public record NotificationReadDto(
	String message,
	NotificationType notificationType
) {
}
