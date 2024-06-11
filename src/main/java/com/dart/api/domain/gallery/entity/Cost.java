package com.dart.api.domain.gallery.entity;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum Cost {
	ALL("all"),
	FREE("free"),
	PAY("pay");

	private final String value;

	Cost(String value) {
		this.value = value;
	}

	private static final Map<String, Cost> valuesMap = Collections.unmodifiableMap(Stream.of(values())
		.collect(Collectors.toMap(Cost::getValue, Function.identity())));

	private String getValue() {
		return value;
	}

	public static Cost fromValue(String value) {
		return valuesMap.get(value);
	}
}
