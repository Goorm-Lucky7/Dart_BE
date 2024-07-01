package com.dart.api.application.notification;

import static com.dart.global.common.util.SSEConstant.*;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.dart.api.domain.auth.entity.AuthUser;
import com.dart.api.domain.member.repository.MemberRepository;
import com.dart.api.domain.notification.entity.Notification;
import com.dart.api.domain.notification.repository.SSESessionRepository;
import com.dart.api.dto.notification.response.NotificationReadDto;
import com.dart.global.error.exception.UnauthorizedException;
import com.dart.global.error.model.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SSENotificationService {

	private final MemberRepository memberRepository;
	private final SSESessionRepository sseSessionRepository;

	public SseEmitter subscribe(AuthUser authUser) {
		final Long memberId = getMemberIdFromAuthUser(authUser);

		SseEmitter sseEmitter = sseSessionRepository.saveSSEEmitter(memberId, SSE_DEFAULT_TIMEOUT);
		log.info("[✅ LOGGER] SUBSCRIBED CLIENT ID: {}", memberId);

		NotificationReadDto notificationReadDto = Notification.createNotificationReadDto(
			"SSE에 성공적으로 연결되었습니다.", null
		);
		sseSessionRepository.sendEvent(memberId, notificationReadDto);

		return sseEmitter;
	}

	private Long getMemberIdFromAuthUser(AuthUser authUser) {
		return memberRepository.findByEmail(authUser.email())
			.orElseThrow(() -> new UnauthorizedException(ErrorCode.FAIL_LOGIN_REQUIRED))
			.getId();
	}
}
