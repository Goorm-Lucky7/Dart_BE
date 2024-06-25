package com.dart.api.domain.notification.repository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Repository;

@Repository
public class PendingEventsRepository {

	// 클라이언트 식별자와 이벤트 목록을 관리
	private final Map<Long, List<Object>> pendingEventsDB = new ConcurrentHashMap<>();

	// 대기 중인 이벤트 추가
	public void savePendingEventCache(Long clientId, Object event) {
		pendingEventsDB.computeIfAbsent(clientId, clientIdKey -> new CopyOnWriteArrayList<>()).add(event);
	}

	// 대기 중인 이벤트 조회
	public List<Object> getPendingEvents(Long clientId) {
		return pendingEventsDB.getOrDefault(clientId, new CopyOnWriteArrayList<>());
	}

	// 대기 중인 이벤트 제거
	public void clearPendingEvents(Long clientId) {
		pendingEventsDB.remove(clientId);
	}
}
