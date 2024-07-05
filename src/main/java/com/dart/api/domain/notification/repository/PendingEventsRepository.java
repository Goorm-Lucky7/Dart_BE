package com.dart.api.domain.notification.repository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Repository;

import com.dart.api.dto.notification.response.NotificationReadDto;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class PendingEventsRepository {

	public final Map<Long, List<NotificationReadDto>> pendingEventsDB = new ConcurrentHashMap<>();

	public void savePendingEventCache(Long clientId, NotificationReadDto notificationReadDto) {
		pendingEventsDB.computeIfAbsent(clientId, clientIdKey -> new CopyOnWriteArrayList<>()).add(notificationReadDto);
		log.info("[✅ LOGGER] PENDING EVENT SAVED FOR CLIENT ID: {} WITH EVENT ID: {}", clientId, notificationReadDto.eventId());
	}

	public List<NotificationReadDto> getPendingEvents(Long clientId) {
		return pendingEventsDB.getOrDefault(clientId, new CopyOnWriteArrayList<>());
	}

	public void clearPendingEvents(Long clientId) {
		pendingEventsDB.remove(clientId);
		log.info("[✅ LOGGER] PENDING EVENTS CLEARED FOR CLIENT ID: {}", clientId);
	}
}
