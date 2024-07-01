package com.dart.api.domain.notification.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum NotificationType {

	LIVE("live"),
	COUPON("coupon");

	private final String name;
}
