package com.dart.api.application.notification;

import static com.dart.global.common.util.SSEConstant.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.dart.api.domain.auth.entity.AuthUser;
import com.dart.api.domain.member.entity.Member;
import com.dart.api.domain.member.repository.MemberRepository;
import com.dart.api.domain.notification.repository.PendingEventsRepository;
import com.dart.api.domain.notification.repository.SSESessionRepository;
import com.dart.api.dto.notification.response.NotificationReadDto;
import com.dart.global.error.exception.UnauthorizedException;
import com.dart.support.AuthFixture;
import com.dart.support.MemberFixture;

@ExtendWith(MockitoExtension.class)
class SSENotificationServiceTest {

	@Mock
	private MemberRepository memberRepository;

	@Mock
	private SSESessionRepository sseSessionRepository;

	@Mock
	private PendingEventsRepository pendingEventsRepository;

	@InjectMocks
	private SSENotificationService sseNotificationService;

	@Test
	@DisplayName("SUBSCRIBE SSE(⭕️ SUCCESS): 성공적으로 SSE에 연결 및 구독했습니다.")
	void subscribe_void_success() {
		// GIVEN
		AuthUser authUser = AuthFixture.createAuthUserEntity();
		Member member = MemberFixture.createMemberEntity();
		SseEmitter sseEmitter = new SseEmitter();

		given(memberRepository.findByEmail(authUser.email())).willReturn(Optional.of(member));
		given(sseSessionRepository.saveSSEEmitter(eq(member.getId()), eq(SSE_DEFAULT_TIMEOUT))).willReturn(sseEmitter);
		doNothing().when(sseSessionRepository).sendEvent(eq(member.getId()), any(NotificationReadDto.class));

		// WHEN
		SseEmitter actualSseEmitter = sseNotificationService.subscribe(authUser, null);

		// THEN
		assertThat(actualSseEmitter).isEqualTo(sseEmitter);
		verify(sseSessionRepository).sendEvent(eq(member.getId()), any(NotificationReadDto.class));
	}

	@Test
	@DisplayName("SUBSCRIBE SSE WITH PENDING EVENTS(⭕️ SUCCESS): 대기 중인 이벤트와 함께 SSE에 성공적으로 연결 및 구독했습니다.")
	void subscribe_withPendingEvents_success() {
		// GIVEN
		String lastEventId = "last-event-id";
		AuthUser authUser = AuthFixture.createAuthUserEntity();
		Member member = MemberFixture.createMemberEntity();
		SseEmitter sseEmitter = new SseEmitter();

		NotificationReadDto pendingEvent1 = new NotificationReadDto("event-1", "message1", "type1");
		NotificationReadDto pendingEvent2 = new NotificationReadDto("event-2", "message2", "type2");

		List<NotificationReadDto> pendingEvents = List.of(pendingEvent1, pendingEvent2);

		given(memberRepository.findByEmail(authUser.email())).willReturn(Optional.of(member));
		given(sseSessionRepository.saveSSEEmitter(eq(member.getId()), eq(SSE_DEFAULT_TIMEOUT))).willReturn(sseEmitter);
		given(pendingEventsRepository.getPendingEvents(eq(member.getId()))).willReturn(pendingEvents);

		// WHEN
		SseEmitter actualSseEmitter = sseNotificationService.subscribe(authUser, lastEventId);

		// THEN
		assertThat(actualSseEmitter).isEqualTo(sseEmitter);
		verify(sseSessionRepository).sendEvent(eq(member.getId()), any(NotificationReadDto.class));
		verify(pendingEventsRepository).getPendingEvents(eq(member.getId()));
		verify(pendingEventsRepository).clearPendingEvents(eq(member.getId()));
	}

	@Test
	@DisplayName("SUBSCRIBE SSE(❌ FAILURE): 존재하지 않은 사용자 이메일로 SSE 연결 및 구독을 시도했습니다.")
	void subscribe_UnauthorizedException_fail() {
		// GIVEN
		AuthUser authUser = AuthFixture.createAuthUserEntity();

		given(memberRepository.findByEmail(authUser.email())).willReturn(Optional.empty());

		// WHEN & THEN
		assertThatThrownBy(() -> sseNotificationService.subscribe(authUser, null))
			.isInstanceOf(UnauthorizedException.class)
			.hasMessage("[❎ ERROR] 로그인이 필요한 기능입니다.");
	}
}
