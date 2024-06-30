package com.dart.api.infrastructure.websocket;

import static com.dart.global.common.util.ChatConstant.*;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.AbstractSubProtocolEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import com.dart.api.domain.auth.entity.AuthUser;
import com.dart.api.domain.member.entity.Member;
import com.dart.api.domain.member.repository.MemberRepository;
import com.dart.global.error.exception.BadRequestException;
import com.dart.global.error.exception.UnauthorizedException;
import com.dart.global.error.model.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

	private final MemberSessionRegistry memberSessionRegistry;
	private final MemberRepository memberRepository;

	@EventListener
	public void handleSubscribeEvent(SessionSubscribeEvent sessionSubscribeEvent) {
		final String sessionId = extractSessionIdFromHeaderAccessor(sessionSubscribeEvent);
		final String destination = extractDestinationFromHeaderAccessor(sessionSubscribeEvent);

		validateSessionIdPresent(sessionId);
		validateDestinationPresent(destination);

		final AuthUser authUser = extractAuthUserFromAttributes(sessionSubscribeEvent);
		validateAuthUserPresent(authUser);
		log.info("[✅ LOGGER] MEMBER {} IS JOIN CHATROOM", authUser.nickname());

		final Member member = getMemberByEmail(authUser.email());
		memberSessionRegistry.removeSessionByNickname(member.getNickname());
		memberSessionRegistry.addSession(member.getNickname(), sessionId, destination, member.getProfileImageUrl());
	}

	@EventListener
	public void handleDisconnectEvent(SessionDisconnectEvent sessionDisconnectEvent) {
		final String sessionId = extractSessionIdFromHeaderAccessor(sessionDisconnectEvent);

		validateSessionIdPresent(sessionId);

		final AuthUser authUser = extractAuthUserFromAttributes(sessionDisconnectEvent);
		validateAuthUserPresent(authUser);
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

		return (AuthUser)simpMessageHeaderAccessor.getSessionAttributes().get(CHAT_SESSION_USER);
	}

	private void validateSessionIdPresent(String sessionId) {
		if (sessionId == null || sessionId.isEmpty()) {
			throw new BadRequestException(ErrorCode.FAIL_INVALID_SESSION_ID);
		}
	}

	private void validateDestinationPresent(String destination) {
		if (destination == null || destination.isEmpty()) {
			throw new BadRequestException(ErrorCode.FAIL_INVALID_DESTINATION);
		}
	}

	private void validateAuthUserPresent(AuthUser authUser) {
		if (authUser == null) {
			log.error("[✅ LOGGER] ACCESS TOKEN IS EMPTIED OR EXPIRED");
		}
	}

	private Member getMemberByEmail(String email) {
		return memberRepository.findByEmail(email)
			.orElseThrow(() -> new UnauthorizedException(ErrorCode.FAIL_LOGIN_REQUIRED));
	}
}
