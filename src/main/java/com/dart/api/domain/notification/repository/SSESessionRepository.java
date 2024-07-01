package com.dart.api.domain.notification.repository;

import static com.dart.global.common.util.SSEConstant.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.dart.api.dto.notification.response.NotificationReadDto;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class SSESessionRepository {

	private final Map<Long, SseEmitter> sseSessionDB = new ConcurrentHashMap<>();

	public SseEmitter saveSSEEmitter(Long clientId, long timeout) {
		SseEmitter sseEmitter = new SseEmitter(timeout);
		sseSessionDB.put(clientId, sseEmitter);
		log.info("[✅ LOGGER] SAVE SSE EMITTER FOR CLIENT ID: {}", clientId);

		handleSSEEmitter(sseEmitter, clientId);

		return sseEmitter;
	}

	public void sendEvent(Long clientId, NotificationReadDto notificationReadDto) {
		SseEmitter sseEmitter = sseSessionDB.get(clientId);

		if (sseEmitter != null) {
			try {
				sseEmitter.send(SseEmitter.event()
					.name(SSE_EMITTER_EVENT_NAME)
					.data(notificationReadDto, MediaType.APPLICATION_JSON));
			} catch (Exception e) {
				deleteSSEEmitterByClientId(clientId);
			}
		}
	}

	public void sendEventToAll(NotificationReadDto notificationReadDto) {
		sseSessionDB.keySet().forEach(clientId -> sendEvent(clientId, notificationReadDto));
	}

	public void deleteSSEEmitterByClientId(Long clientId) {
		sseSessionDB.remove(clientId);
	}

	private void handleSSEEmitter(SseEmitter sseEmitter, Long clientId) {
		sseEmitter.onCompletion(() -> {
			log.info("[✅ LOGGER] COMPLETION SSE EMITTER FOR CLIENT ID: {}", clientId);
			deleteSSEEmitterByClientId(clientId);
		});
		sseEmitter.onTimeout(() -> {
			log.info("[✅ LOGGER] TIMEOUT SSE EMITTER FOR CLIENT ID: {}", clientId);
			deleteSSEEmitterByClientId(clientId);
		});
		sseEmitter.onError(error -> {
			log.error("[✅ LOGGER] ERROR SSE EMITTER FOR CLIENT ID: {}", clientId, error);
			deleteSSEEmitterByClientId(clientId);
		});
	}

	public void completeSSEEmitter(Long clientId) {
		SseEmitter sseEmitter = sseSessionDB.get(clientId);
		if (sseEmitter != null) {
			sseEmitter.complete();
		}
	}
}
