package com.dart.api.application.coupon;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dart.api.domain.auth.entity.AuthUser;
import com.dart.api.domain.coupon.entity.GeneralCoupon;
import com.dart.api.domain.coupon.entity.PriorityCoupon;
import com.dart.api.domain.coupon.repository.GeneralCouponRepository;
import com.dart.api.domain.coupon.repository.GeneralCouponWalletRepository;
import com.dart.api.domain.coupon.repository.PriorityCouponRepository;
import com.dart.api.domain.member.entity.Member;
import com.dart.api.domain.member.repository.MemberRepository;
import com.dart.api.dto.coupon.response.CouponReadAllDto;
import com.dart.global.common.util.ClockHolder;
import com.dart.support.CouponFixture;
import com.dart.support.MemberFixture;

@ExtendWith({MockitoExtension.class})
class CouponServiceTest {
	@Mock
	private MemberRepository memberRepository;

	@Mock
	private PriorityCouponRepository priorityCouponRepository;

	@Mock
	private GeneralCouponRepository generalCouponRepository;

	@Mock
	private ClockHolder clockHolder;

	@Mock
	private GeneralCouponWalletRepository generalCouponWalletRepository;

	@InjectMocks
	private CouponService couponService;

	@DisplayName("일반 쿠폰과 선착순 쿠폰을 성공적으로 조회한다. - CouponReadAllDto")
	@Test
	void getById_success() {
		// Given
		List<PriorityCoupon> priorityCoupons = CouponFixture.createPriorityCouponList();
		List<GeneralCoupon> generalCoupons = CouponFixture.createGeneralCouponList();
		AuthUser authUser = MemberFixture.createAuthUserEntity();
		LocalDate nowDate = LocalDate.of(2024, 6, 24);

		given(clockHolder.nowDate()).willReturn(nowDate);
		given(priorityCouponRepository.findAllWithStartedAtBeforeOrEqual(any(LocalDate.class))).willReturn(
			priorityCoupons);
		given(generalCouponRepository.findAll()).willReturn(generalCoupons);

		Member member = MemberFixture.createMemberEntity();

		given(memberRepository.findByEmail(authUser.email())).willReturn(Optional.ofNullable(member));
		given(generalCouponWalletRepository.existsByGeneralCouponAndMember(any(GeneralCoupon.class),
			eq(member))).willReturn(true);

		// When
		CouponReadAllDto actual = couponService.readAll(authUser);

		// Then
		assertThat(actual.generalCoupon()).hasSameSizeAs(generalCoupons);
		assertThat(actual.priorityCoupon()).hasSameSizeAs(priorityCoupons);
	}
}
