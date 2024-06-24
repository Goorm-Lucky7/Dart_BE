package com.dart.api.domain.coupon.entity;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CouponEventType {
	MONTHLY_COUPON("이달의쿠폰"),
	WELCOME_COUPON("웰컴쿠폰"),
	LAUNCHING_COUPON("런칭기념쿠폰");

	private final String name;

	private static final Map<String, CouponEventType> namesMap = Collections.unmodifiableMap(Stream.of(values())
		.collect(Collectors.toMap(CouponEventType::getName, Function.identity())));
}
