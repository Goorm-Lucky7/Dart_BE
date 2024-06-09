package com.dart.api.domain.gallery.entity;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum Display {
	UPCOMING("upcoming"),
	INPROGRESS("inprogress"),
	FINISHED("finished");

	private final String value;

	Display(String value) {
		this.value = value;
	}

	private static final Map<String, Display> valueMap = Arrays.stream(values())
		.collect(Collectors.toUnmodifiableMap(Display::getValue, Function.identity()));

	public String getValue() {
		return value;
	}

	public static Display fromValue(String value) {
		return valueMap.get(value);
	}
}
