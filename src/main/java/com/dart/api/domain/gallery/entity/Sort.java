package com.dart.api.domain.gallery.entity;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.dart.global.error.exception.BadRequestException;
import com.dart.global.error.model.ErrorCode;

public enum Sort {
	LATEST("latest"),
	LIKED("liked");

	private final String value;

	Sort(String value) {
		this.value = value;
	}

	private static final Map<String, Sort> valuesMap = Collections.unmodifiableMap(Stream.of(values())
		.collect(Collectors.toMap(Sort::getValue, Function.identity())));

	public String getValue() {
		return value;
	}

	public static Sort fromValue(String value) {
		Sort sort = valuesMap.get(value.toLowerCase());
		if (sort == null) {
			throw new BadRequestException(ErrorCode.FAIL_INVALID_SORT_VALUE);
		}
		return sort;
	}
}
