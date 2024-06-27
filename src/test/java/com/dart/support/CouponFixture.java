package com.dart.support;

import static com.dart.support.MemberFixture.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.params.provider.Arguments;

import com.dart.api.domain.coupon.entity.CouponEventType;
import com.dart.api.domain.coupon.entity.CouponType;
import com.dart.api.domain.coupon.entity.GeneralCoupon;
import com.dart.api.domain.coupon.entity.GeneralCouponWallet;
import com.dart.api.domain.coupon.entity.PriorityCoupon;
import com.dart.api.domain.coupon.entity.PriorityCouponWallet;

public class CouponFixture {
	public static PriorityCoupon createPriorityCoupon() {
		return PriorityCoupon.builder()
			.stock(100)
			.title("오픈기념선착순")
			.startedAt(LocalDate.now().minusDays(1))
			.endedAt(LocalDate.now().plusDays(1))
			.couponType(CouponType.TEN_PERCENT)
			.build();
	}

	public static GeneralCoupon createGeneralCoupon() {
		return new GeneralCoupon("10%할인", CouponType.TEN_PERCENT, CouponEventType.MONTHLY_COUPON);
	}

	public static List<PriorityCoupon> createPriorityCouponList() {
		return List.of(createPriorityCoupon());
	}

	public static List<GeneralCoupon> createGeneralCouponList() {
		return List.of(createGeneralCoupon());
	}

	public static List<PriorityCouponWallet> createPriorityCouponWalletList() {
		return List.of(PriorityCouponWallet.create(createPriorityCoupon(), MemberFixture.createMemberEntity()));
	}

	public static List<GeneralCouponWallet> createGeneralCouponWalletList() {
		return List.of(GeneralCouponWallet.create(createGeneralCoupon(), MemberFixture.createMemberEntity()));
	}

	public static Stream<Arguments> provideValues_String() {
		Set<Object> values = new HashSet<>();
		values.add(1L);
		values.add(2L);
		values.add(3L);
		values.add(4L);
		values.add(5L);
		values.add(6L);
		values.add(7L);
		values.add(8L);
		values.add(9L);
		values.add(10L);

		return Stream.of(Arguments.of(values));
	}

	public static Stream<Arguments> provideGeneralCouponWallet_total5() {
		return Stream.of(Arguments.of(
			List.of(
				GeneralCouponWallet.create(createGeneralCoupon(), createMemberEntity()),
				GeneralCouponWallet.create(createGeneralCoupon(), createMemberEntity()),
				GeneralCouponWallet.create(createGeneralCoupon(), createMemberEntity()),
				GeneralCouponWallet.create(createGeneralCoupon(), createMemberEntity()),
				GeneralCouponWallet.create(createGeneralCoupon(), createMemberEntity())
			))
		);
	}
}
