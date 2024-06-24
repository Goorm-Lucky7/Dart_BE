package com.dart.api.presentation;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.dart.api.application.notification.SSENotificationService;
import com.dart.api.domain.auth.entity.AuthUser;
import com.dart.api.dto.notification.response.NotificationReadDto;
import com.dart.global.auth.annotation.Auth;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

	private final SSENotificationService SSENotificationService;

	@GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public ResponseEntity<SseEmitter> subscribe(@Auth AuthUser authUser) {
		return ResponseEntity.ok(SSENotificationService.subscribe(authUser));
	}

	@GetMapping
	public ResponseEntity<List<NotificationReadDto>> getNotifications() {
		return ResponseEntity.ok(SSENotificationService.getNotifications());
	}
}
