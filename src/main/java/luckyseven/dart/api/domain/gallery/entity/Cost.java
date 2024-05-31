package luckyseven.dart.api.domain.gallery.entity;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum Cost {
	ALL("전체", "all"),
	FREE("무료", "free"),
	PAY("유료", "pay");

	private final String name;
	private final String value;

	Cost(String name, String value) {
		this.name = name;
		this.value = value;
	}

	private static final Map<String, Cost> names = Collections.unmodifiableMap(Stream.of(values())
		.collect(Collectors.toMap(Cost::getName, Function.identity())));

	private static final Map<String, Cost> valuesMap = Collections.unmodifiableMap(Stream.of(values())
		.collect(Collectors.toMap(Cost::getValue, Function.identity())));

	private String getName() {
		return name;
	}

	private String getValue() {
		return value;
	}

	public static boolean isValidCost(String value) {
		return valuesMap.containsKey(value);
	}

	public static Cost fromValue(String value) {
		return valuesMap.get(value);
	}
}
