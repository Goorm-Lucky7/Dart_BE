package com.dart.api.infrastructure.websocket;

import java.util.Objects;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.AbstractSubProtocolEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import com.dart.api.domain.auth.entity.AuthUser;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

	private final MemberSessionRegistry memberSessionRegistry;

	@EventListener
	public void handleSubscribeEvent(SessionSubscribeEvent sessionSubscribeEvent) {
		log.info("[✅ LOGGER] HANDLE SUBSCRIBE EVENT CALLED");
		final String sessionId = extractSessionIdFromHeaderAccessor(sessionSubscribeEvent);
		final String destination = extractDestinationFromHeaderAccessor(sessionSubscribeEvent);

		AuthUser authUser = extractAuthUserFromAttributes(sessionSubscribeEvent);
		log.info("[✅ LOGGER] MEMBER {} IS JOIN CHATROOM", authUser.nickname());

		memberSessionRegistry.addSession(authUser.nickname(), sessionId, destination);
	}

	@EventListener
	public void handleUnsubscribeEvent(SessionUnsubscribeEvent sessionUnsubscribeEvent) {
		log.info("[✅ LOGGER] HANDLE UNSUBSCRIBE EVENT CALLED");
		final String sessionId = extractSessionIdFromHeaderAccessor(sessionUnsubscribeEvent);

		AuthUser authUser = extractAuthUserFromAttributes(sessionUnsubscribeEvent);
		log.info("[✅ LOGGER] MEMBER {} IS LEFT CHATROOM", authUser.nickname());

		memberSessionRegistry.removeSession(sessionId);
	}

	private String extractSessionIdFromHeaderAccessor(AbstractSubProtocolEvent abstractSubProtocolEvent) {
		return SimpMessageHeaderAccessor.wrap(abstractSubProtocolEvent.getMessage()).getSessionId();
	}

	private String extractDestinationFromHeaderAccessor(AbstractSubProtocolEvent abstractSubProtocolEvent) {
		return SimpMessageHeaderAccessor.wrap(abstractSubProtocolEvent.getMessage()).getDestination();
	}

	private AuthUser extractAuthUserFromAttributes(AbstractSubProtocolEvent abstractSubProtocolEvent) {
		SimpMessageHeaderAccessor simpMessageHeaderAccessor = SimpMessageHeaderAccessor
			.wrap(abstractSubProtocolEvent.getMessage());

		return (AuthUser)Objects.requireNonNull(simpMessageHeaderAccessor.getSessionAttributes()).get("authMember");
	}
}
