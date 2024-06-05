package com.dart.api.infrastructure.websocket;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.dart.api.dto.chat.response.MemberSessionDto;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class MemberSessionRegistry {

	private final Map<String, MemberSessionDto> memberSessionRegistry = new ConcurrentHashMap<>();

	public void addSession(String nickname, String sessionId, String destination) {
		MemberSessionDto memberSessionDto = new MemberSessionDto(nickname, sessionId, destination);
		memberSessionRegistry.put(sessionId, memberSessionDto);

		log.info("[✅ LOGGER] SESSION ADDED: {}", memberSessionDto);
	}

	public void removeSession(String sessionId) {
		memberSessionRegistry.remove(sessionId);

		log.info("[✅ LOGGER] SESSION REMOVED: sessionId={}", sessionId);
	}

	public List<String> getMembersInChatRoom(String destination) {
		List<String> members = memberSessionRegistry.values().stream()
			.filter(session -> session.destination().equals(destination))
			.map(MemberSessionDto::nickname)
			.toList();

		log.info("[✅ LOGGER] MEMBERS IN {}: {}", destination, members);

		return members;
	}
}
