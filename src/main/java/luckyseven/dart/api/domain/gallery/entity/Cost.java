package luckyseven.dart.api.domain.gallery.entity;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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

	private static Map<String, Cost> names = new HashMap<>();
	private static Map<String, Cost> valuesMap = new HashMap<>();

	static {
		for (Cost cost : Cost.values()) {
			names.put(cost.name, cost);
			valuesMap.put(cost.value, cost);
		}
		names = Collections.unmodifiableMap(names);
		valuesMap = Collections.unmodifiableMap(valuesMap);
	}

	private String getValue() {
		return value;
	}

	public static Cost fromValue(String value) {
		return valuesMap.get(value);
	}

	public static boolean isValidCost(String value) {
		for (Cost cost : Cost.values()) {
			if (cost.getValue().equalsIgnoreCase(value)) {
				return true;
			}
		}
		return false;
	}
}
