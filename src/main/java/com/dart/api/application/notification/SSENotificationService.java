package com.dart.api.application.notification;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.dart.api.domain.auth.entity.AuthUser;
import com.dart.api.domain.member.repository.MemberRepository;
import com.dart.api.domain.notification.entity.Notification;
import com.dart.api.domain.notification.repository.NotificationRepository;
import com.dart.api.domain.notification.repository.SSESessionRepository;
import com.dart.api.dto.notification.response.NotificationReadDto;
import com.dart.global.error.exception.UnauthorizedException;
import com.dart.global.error.model.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SSENotificationService {

	private final MemberRepository memberRepository;
	private final NotificationRepository notificationRepository;
	private final SSESessionRepository sseSessionRepository;

	public SseEmitter subscribe(AuthUser authUser) {
		Long memberId = getMemberIdFromAuthUser(authUser);

		SseEmitter sseEmitter = sseSessionRepository.saveSSEEmitter(memberId);
		log.info("[✅ LOGGER] SUBSCRIBED CLIENT ID: {}", memberId);

		sseSessionRepository.sendEvent(memberId, "DUMMY_EVENT", "CONNECT SSE");
		log.info("[✅ LOGGER] DUMMY EVENT SENT TO SSE EMITTER");

		return sseEmitter;
	}

	public List<NotificationReadDto> getNotifications() {
		return notificationRepository.findAll().stream()
			.map(Notification::toNotificationReadDto)
			.toList();
	}

	private Long getMemberIdFromAuthUser(AuthUser authUser) {
		return memberRepository.findByEmail(authUser.email())
			.orElseThrow(() -> new UnauthorizedException(ErrorCode.FAIL_LOGIN_REQUIRED))
			.getId();
	}
}
