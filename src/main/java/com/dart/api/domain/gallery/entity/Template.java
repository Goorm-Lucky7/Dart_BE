package com.dart.api.domain.gallery.entity;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Template {
	ONE("one"),
	TWO("two"),
	THREE("three"),
	FOUR("four");

	private final String value;

	private static final Map<String, Template> valuesMap = Collections.unmodifiableMap(Stream.of(values())
		.collect(Collectors.toMap(Template::getValue, Function.identity())));

	private String getValue() {
		return value;
	}

	public static Template fromValue(String value) {
		return valuesMap.get(value);
	}

	public static boolean isValidTemplate(String value) {
		for (Template template : Template.values()) {
			if (template.getValue().equalsIgnoreCase(value)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return value;
	}
}
