package com.dart.api.domain.gallery.entity;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum Category {
	HASHTAG("hashtag"),
	AUTHOR("author"),
	TITLE("title");

	private final String value;
	private static final Map<String, Category> valueMap;

	Category(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	static {
		valueMap = Collections.unmodifiableMap(Stream.of(values())
			.collect(Collectors.toMap(Category::getValue, Function.identity())));
	}

	public static Category fromValue(String value) {
		return valueMap.get(value.toLowerCase());
	}
}
