package com.dart.api.domain.notification.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum NotificationType {

	COUPON_START("쿠폰 발행"),
	REEXHIBITION_REQUEST("재전시 요청");

	private final String name;
}
