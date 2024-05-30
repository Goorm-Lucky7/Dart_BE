package luckyseven.dart.api.domain.payment.entity;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Order {
	TICKET("ticket"),
	PAID_GALLERY("paidGallery");

	private final String value;

	private static final Map<String, Order> valuesMap = Collections.unmodifiableMap(Stream.of(values())
		.collect(Collectors.toMap(Order::getValue, Function.identity())));

	public static Order fromValue(String value) {
		return valuesMap.get(value);
	}
}
