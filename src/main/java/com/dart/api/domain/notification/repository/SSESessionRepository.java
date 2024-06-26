package com.dart.api.domain.notification.repository;

import static com.dart.global.common.util.SSEConstant.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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

	public void sendEvent(Long clientId, Object event, String comment) {
		SseEmitter sseEmitter = sseSessionDB.get(clientId);

		if (sseEmitter != null) {
			try {
				sseEmitter.send(SseEmitter.event()
					.id(String.valueOf(clientId))
					.name(SSE_EMITTER_EVENT_NAME)
					.data(event, MediaType.APPLICATION_JSON)
					.comment(comment));
				log.info("[✅ LOGGER] EVENT SENT TO CLIENT ID: {}", clientId);
			} catch (Exception e) {
				deleteSSEEmitterByClientId(clientId);
				log.error("[✅ LOGGER] FAILED TO SEND EVENT TO CLIENT ID: {}", clientId, e);
			}
		}
	}

	public void sendEventToAll(Object event, String comment) {
		sseSessionDB.keySet().forEach(clientId -> sendEvent(clientId, event, comment));
	}

	public void deleteSSEEmitterByClientId(Long clientId) {
		sseSessionDB.remove(clientId);
		log.info("[✅ LOGGER] DELETE SSE EMITTER FOR CLIENT ID: {}", clientId);
	}

	private void handleSSEEmitter(SseEmitter sseEmitter, Long clientId) {
		sseEmitter.onCompletion(() -> deleteSSEEmitterByClientId(clientId));
		sseEmitter.onTimeout(() -> deleteSSEEmitterByClientId(clientId));
		sseEmitter.onError(error -> deleteSSEEmitterByClientId(clientId));
	}
}
