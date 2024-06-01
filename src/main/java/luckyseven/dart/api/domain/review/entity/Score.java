package luckyseven.dart.api.domain.review.entity;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Score {
	ONE_STAR(1),
	TWO_STAR(2),
	THREE_STAR(3),
	FOUR_STAR(4),
	FIVE_STAR(5);

	private final int value;

	private static final Map<Integer, Score> valuesMap = Collections.unmodifiableMap(Stream.of(values())
		.collect(Collectors.toMap(Score::getValue, Function.identity())));

	public static Score fromValue(int value) {
		return valuesMap.get(value);
	}
}
