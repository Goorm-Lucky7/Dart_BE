package com.dart.api.domain.payment.entity;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderType {
	TICKET("ticket"),
	PAID_GALLERY("paidGallery");

	private final String value;

	private static final Map<String, OrderType> valuesMap = Collections.unmodifiableMap(Stream.of(values())
		.collect(Collectors.toMap(OrderType::getValue, Function.identity())));

	public static OrderType fromValue(String value) {
		return valuesMap.get(value);
	}

	public static boolean contains(String value) {
		return valuesMap.containsKey(value);
	}
}
