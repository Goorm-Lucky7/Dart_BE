package com.dart.api.dto.notification.response;

import java.time.LocalDateTime;

import com.dart.api.domain.notification.entity.NotificationType;

import lombok.Builder;

@Builder
public record NotificationReadDto(
	LocalDateTime createdAt,
	String message,
	NotificationType notificationType
) {
}
