package com.dart.api.application.gallery;

import static com.dart.global.common.util.GlobalConstant.*;
import static com.dart.global.common.util.SSEConstant.*;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.dart.api.domain.auth.entity.AuthUser;
import com.dart.api.domain.member.repository.MemberRepository;
import com.dart.api.domain.notification.repository.SSESessionRepository;
import com.dart.global.error.exception.UnauthorizedException;
import com.dart.global.error.model.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GalleryProgressService {

	private final SSESessionRepository sseSessionRepository;
	private final MemberRepository memberRepository;

	public SseEmitter createEmitter(AuthUser authUser) {
		Long clientId = getMemberIdFromAuthUser(authUser);
		return sseSessionRepository.saveSSEEmitter(clientId, SSE_CREATE_GALLERY_TIMEOUT);
	}

	public void sendProgress(Long clientId, int progress) {
		sseSessionRepository.sendEvent(clientId, progress, "Progress Update");
		if (progress == ONE_HUNDRED_PERCENT) {
			sseSessionRepository.deleteSSEEmitterByClientId(clientId);
		}
	}

	private Long getMemberIdFromAuthUser(AuthUser authUser) {
		return memberRepository.findByEmail(authUser.email())
			.orElseThrow(() -> new UnauthorizedException(ErrorCode.FAIL_LOGIN_REQUIRED))
			.getId();
	}
}
