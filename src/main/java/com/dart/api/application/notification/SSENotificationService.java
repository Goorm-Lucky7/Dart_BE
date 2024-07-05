package com.dart.api.application.notification;

import static com.dart.global.common.util.SSEConstant.*;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.dart.api.domain.auth.entity.AuthUser;
import com.dart.api.domain.member.repository.MemberRepository;
import com.dart.api.domain.notification.entity.Notification;
import com.dart.api.domain.notification.repository.PendingEventsRepository;
import com.dart.api.domain.notification.repository.SSESessionRepository;
import com.dart.api.dto.notification.response.NotificationReadDto;
import com.dart.global.error.exception.UnauthorizedException;
import com.dart.global.error.model.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SSENotificationService {

	private final MemberRepository memberRepository;
	private final SSESessionRepository sseSessionRepository;
	private final PendingEventsRepository pendingEventsRepository;

	public SseEmitter subscribe(AuthUser authUser, String lastEventId) {
		final Long memberId = getMemberIdFromAuthUser(authUser);
		final SseEmitter sseEmitter = sseSessionRepository.saveSSEEmitter(memberId, SSE_DEFAULT_TIMEOUT);
		final NotificationReadDto notificationReadDto = Notification.createNotificationReadDto(
			SSE_CONNECTION_SUCCESS_MESSAGE, null);

		sseSessionRepository.sendEvent(memberId, notificationReadDto);

		sendPendingEventsIfAny(memberId, lastEventId);

		return sseEmitter;
	}

	private void sendPendingEventsIfAny(Long memberId, String lastEventId) {
		if (lastEventId != null && !lastEventId.isEmpty()) {
			pendingEventsRepository.getPendingEvents(memberId).stream()
				.filter(pendingEvent -> lastEventId.compareTo(pendingEvent.eventId()) < EVENT_ID_COMPARISON_RESULT)
				.forEach(pendingEvent -> sseSessionRepository.sendEvent(memberId, pendingEvent));

			pendingEventsRepository.clearPendingEvents(memberId);
		}
	}

	private Long getMemberIdFromAuthUser(AuthUser authUser) {
		return memberRepository.findByEmail(authUser.email())
			.orElseThrow(() -> new UnauthorizedException(ErrorCode.FAIL_LOGIN_REQUIRED))
			.getId();
	}
}
