package com.dart.support;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.params.provider.Arguments;

import com.dart.api.domain.coupon.entity.CouponType;
import com.dart.api.domain.coupon.entity.PriorityCoupon;

public class CouponFixture {
	public static PriorityCoupon create() {
		return PriorityCoupon.builder()
			.stock(100)
			.title("오픈기념선착순")
			.startedAt(LocalDate.now().minusDays(1))
			.endedAt(LocalDate.now().plusDays(1))
			.couponType(CouponType.TEN_PERCENT)
			.build();
	}

	public static Stream<Arguments> provideValues_String() {
		Set<Object> values = new HashSet<>();
		values.add("test1@naver.com");
		values.add("test2@naver.com");
		values.add("test3@naver.com");
		values.add("test4@naver.com");
		values.add("test5@naver.com");
		values.add("test6@naver.com");
		values.add("test7@naver.com");
		values.add("test8@naver.com");
		values.add("test9@naver.com");
		values.add("test10@naver.com");

		return Stream.of(Arguments.of(values));
	}
}
