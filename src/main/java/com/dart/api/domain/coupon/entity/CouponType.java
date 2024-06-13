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
public enum CouponType {
	TEN_PERCENT(10),
	TWENTY_PERCENT(20),
	THIRTY_PERCENT(30);

	private final int value;

	private static final Map<Integer, CouponType> valuesMap = Collections.unmodifiableMap(Stream.of(values())
		.collect(Collectors.toMap(CouponType::getValue, Function.identity())));
}
