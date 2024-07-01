package com.dart.api.domain.notification.entity;

import com.dart.api.dto.notification.response.NotificationReadDto;
import com.dart.global.common.entity.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "tbl_notification")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseTimeEntity {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "message", nullable = false)
	private String message;

	@Column(name = "notification_type", nullable = false)
	@Enumerated(EnumType.STRING)
	private NotificationType notificationType;

	@Builder
	private Notification(String message, NotificationType notificationType) {
		this.message = message;
		this.notificationType = notificationType;
	}

	public static NotificationReadDto createNotificationReadDto(String message, String type) {
		return NotificationReadDto.builder()
			.message(message)
			.type(type)
			.build();
	}
}
