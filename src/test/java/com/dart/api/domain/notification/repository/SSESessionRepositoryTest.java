package com.dart.api.domain.notification.repository;

import static com.dart.global.common.util.SSEConstant.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.dart.api.dto.notification.response.NotificationReadDto;
import com.dart.support.NotificationFixture;

@ExtendWith(MockitoExtension.class)
class SSESessionRepositoryTest {

	@Mock
	private SseEmitter sseEmitter;

	@InjectMocks
	private SSESessionRepository sseSessionRepository;

	@BeforeEach
	void setUp() {
		sseSessionRepository.sseSessionDB.clear();
	}

	@Test
	@DisplayName("SAVE SSE EMITTER(⭕️ SUCCESS): 성공적으로 SSE Emitter 객체를 저장했습니다.")
	void saveSSEEmitter_void_success() {
		// GIVEN
		Long clientID = 1L;

		// WHEN
		SseEmitter sseEmitter = sseSessionRepository.saveSSEEmitter(clientID, SSE_DEFAULT_TIMEOUT);

		// THEN
		assertThat(sseSessionRepository.sseSessionDB.containsKey(clientID)).isTrue();
		assertThat(sseSessionRepository.sseSessionDB.get(clientID)).isEqualTo(sseEmitter);
	}

	@Test
	@DisplayName("SEND EVENT(⭕️ SUCCESS): 성공적으로 SSE Emitter 이벤트를 클라이언트에게 전송했습니다.")
	void sendEvent_success() throws IOException {
		// GIVEN
		Long clientID = 1L;

		NotificationReadDto notificationReadDto = NotificationFixture.createNotificationReadDto(null);

		sseSessionRepository.sseSessionDB.put(clientID, sseEmitter);

		// WHEN
		sseSessionRepository.sendEvent(clientID, notificationReadDto);

		// THEN
		verify(sseEmitter, times(1)).send(any(SseEmitter.SseEventBuilder.class));
	}

	@Test
	@DisplayName("SEND EVENT TO ALL(⭕️ SUCCESS): 성공적으로 모든 SSE Emitter 이벤트를 클라이언트들에게 전송했습니다.")
	void sendEventToAll_void_success() throws IOException {
		// GIVEN
		Long clientID1 = 1L;
		Long clientID2 = 2L;

		SseEmitter sseEmitter1 = mock(SseEmitter.class);
		SseEmitter sseEmitter2 = mock(SseEmitter.class);

		NotificationReadDto notificationReadDto = NotificationFixture.createNotificationReadDto(null);

		sseSessionRepository.sseSessionDB.put(clientID1, sseEmitter1);
		sseSessionRepository.sseSessionDB.put(clientID2, sseEmitter2);

		// WHEN
		sseSessionRepository.sendEventToAll(notificationReadDto);

		// THEN
		verify(sseEmitter1, times(1)).send(any(SseEmitter.SseEventBuilder.class));
		verify(sseEmitter2, times(1)).send(any(SseEmitter.SseEventBuilder.class));
	}

	@Test
	@DisplayName("DELETE SSE EMITTER(⭕️ SUCCESS): 성공적으로 SSE Emitter 객체를 삭제했습니다.")
	void deleteSSEEmitterByClientId_void_success() {
		// GIVEN
		Long clientID = 1L;

		sseSessionRepository.sseSessionDB.put(clientID, sseEmitter);

		// WHEN
		sseSessionRepository.deleteSSEEmitterByClientId(clientID);

		// THEN
		assertThat(sseSessionRepository.sseSessionDB.containsKey(clientID)).isFalse();
	}
}
