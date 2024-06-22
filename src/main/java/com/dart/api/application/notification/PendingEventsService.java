package com.dart.api.application.notification;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Service;

@Service
public class PendingEventsService {

	private final Map<String, List<Object>> pendingEventsDB = new ConcurrentHashMap<>();

	public void addEvent(String clientId, Object event) {
		pendingEventsDB.computeIfAbsent(clientId, clientIdKey -> new CopyOnWriteArrayList<>()).add(event);
	}

	public List<Object> getPendingEvents(String clientId) {
		return pendingEventsDB.getOrDefault(clientId, new CopyOnWriteArrayList<>());
	}

	public void clearPendingEvents(String clientId) {
		pendingEventsDB.remove(clientId);
	}
}
