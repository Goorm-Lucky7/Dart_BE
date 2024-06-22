package com.dart.api.application.notification;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class SSESessionService {

	private final Map<String, SseEmitter> sseSessionDB = new ConcurrentHashMap<>();

	public SseEmitter register(String clientId) {
		SseEmitter sseEmitter = new SseEmitter();
		sseSessionDB.put(clientId, sseEmitter);

		sseEmitter.onCompletion(() -> sseSessionDB.remove(clientId));
		sseEmitter.onTimeout(() -> sseSessionDB.remove(clientId));
		sseEmitter.onError(error -> sseSessionDB.remove(clientId));

		return sseEmitter;
	}
}
