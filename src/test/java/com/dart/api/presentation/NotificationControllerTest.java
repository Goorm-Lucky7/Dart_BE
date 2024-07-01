package com.dart.api.presentation;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.dart.api.application.notification.SSENotificationService;
import com.dart.api.domain.auth.entity.AuthUser;
import com.dart.support.AuthFixture;

@ExtendWith(SpringExtension.class)
class NotificationControllerTest {

	@Mock
	private SSENotificationService sseNotificationService;

	@InjectMocks
	private NotificationController notificationController;

	@Mock
	private SseEmitter sseEmitter;

	@Test
	@DisplayName("SUBSCRIBE SSE(⭕️ SUCCESS): 성공적으로 SSE에 연결 및 구독했습니다.")
	void subscribe_void_success() {
		// GIVEN
		AuthUser authUser = AuthFixture.createAuthUserEntity();
		when(sseNotificationService.subscribe(any(AuthUser.class))).thenReturn(sseEmitter);

		// WHEN
		ResponseEntity<SseEmitter> responseEntity = notificationController.subscribe(authUser);

		// THEN
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(responseEntity.getBody()).isEqualTo(sseEmitter);
	}
}
