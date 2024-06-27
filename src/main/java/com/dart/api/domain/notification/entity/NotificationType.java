package com.dart.api.domain.notification.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum NotificationType {

	LIVE("실시간 쿠폰 이벤트"),
	COUPON("쿠폰 발행");

	private final String name;
}
