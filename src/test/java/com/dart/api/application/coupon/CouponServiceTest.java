package com.dart.api.application.coupon;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDate;
import java.util.Collections;
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
import com.dart.api.domain.coupon.entity.GeneralCouponWallet;
import com.dart.api.domain.coupon.entity.PriorityCoupon;
import com.dart.api.domain.coupon.entity.PriorityCouponWallet;
import com.dart.api.domain.coupon.repository.GeneralCouponRepository;
import com.dart.api.domain.coupon.repository.GeneralCouponWalletRepository;
import com.dart.api.domain.coupon.repository.PriorityCouponRepository;
import com.dart.api.domain.coupon.repository.PriorityCouponWalletRepository;
import com.dart.api.domain.member.entity.Member;
import com.dart.api.domain.member.repository.MemberRepository;
import com.dart.api.dto.coupon.response.CouponReadAllDto;
import com.dart.api.dto.coupon.response.MyCouponReadAllDto;
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

	@Mock
	private PriorityCouponWalletRepository priorityCouponWalletRepository;

	@InjectMocks
	private CouponService couponService;

	@DisplayName("일반 쿠폰과 선착순 쿠폰을 성공적으로 조회한다. - CouponReadAllDto")
	@Test
	void readAll_success() {
		// Given
		List<PriorityCoupon> priorityCoupons = CouponFixture.createPriorityCouponList();
		List<GeneralCoupon> generalCoupons = CouponFixture.createGeneralCouponList();
		AuthUser authUser = MemberFixture.createAuthUserEntity();
		LocalDate nowDate = LocalDate.now();

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

	@DisplayName("내가 가지고 있는 일반 쿠폰과 선착순 쿠폰을 성공적으로 조회한다. - MyCouponReadAllDto")
	@Test
	void readAllMyCoupon_success() {
		// Given
		List<PriorityCouponWallet> priorityCouponWallets = CouponFixture.createPriorityCouponWalletList();
		List<GeneralCouponWallet> generalCouponWallets = CouponFixture.createGeneralCouponWalletList();
		AuthUser authUser = MemberFixture.createAuthUserEntity();
		Member member = MemberFixture.createMemberEntity();

		given(memberRepository.findByEmail(authUser.email())).willReturn(Optional.ofNullable(member));
		given(priorityCouponWalletRepository.findByMemberAndIsUsedFalse(any(Member.class))).willReturn(
			priorityCouponWallets);
		given(generalCouponWalletRepository.findByMemberAndIsUsedFalse(any(Member.class))).willReturn(
			generalCouponWallets);

		// When
		MyCouponReadAllDto actual = couponService.readAllMyCoupon(authUser);

		// Then
		int expectedSize = priorityCouponWallets.size() + generalCouponWallets.size();
		assertThat(actual.myCoupons()).hasSize(expectedSize);
	}

	@DisplayName("이미 발급받은 일반 쿠폰을 조회한다. - isAlreadyCoupon")
	@Test
	void readAll_hasCoupon_true() {
		// Given
		List<GeneralCoupon> generalCoupons = CouponFixture.createGeneralCouponList();
		AuthUser authUser = MemberFixture.createAuthUserEntity();
		LocalDate nowDate = LocalDate.now();

		given(clockHolder.nowDate()).willReturn(nowDate);
		given(generalCouponRepository.findAll()).willReturn(generalCoupons);

		Member member = MemberFixture.createMemberEntity();

		given(memberRepository.findByEmail(authUser.email())).willReturn(Optional.ofNullable(member));
		given(generalCouponWalletRepository.existsByGeneralCouponAndMember(any(GeneralCoupon.class),
			eq(member))).willReturn(true);

		// When
		CouponReadAllDto actual = couponService.readAll(authUser);

		// Then
		assertThat(actual.generalCoupon().get(0).hasCoupon()).isTrue();
	}

	@DisplayName("비로그인 사용자는 가지고 있는 쿠폰이 없다 - isAlreadyCoupon")
	@Test
	void readAll_hasCoupon_false() {
		// Given
		List<GeneralCoupon> generalCoupons = CouponFixture.createGeneralCouponList();
		LocalDate nowDate = LocalDate.now();

		given(clockHolder.nowDate()).willReturn(nowDate);
		given(generalCouponRepository.findAll()).willReturn(generalCoupons);

		// When
		CouponReadAllDto actual = couponService.readAll(null);

		// Then
		assertThat(actual.generalCoupon().get(0).hasCoupon()).isFalse();
	}

	@DisplayName("해당 선착순 쿠폰은 종료된 쿠폰이다. - isFinished")
	@Test
	void readAll_isFinished_true() {
		// Given
		List<PriorityCoupon> priorityCoupons = CouponFixture.createPriorityCouponList();
		AuthUser authUser = MemberFixture.createAuthUserEntity();
		LocalDate nowDate = LocalDate.now().plusDays(2);

		given(clockHolder.nowDate()).willReturn(nowDate);
		given(priorityCouponRepository.findAllWithStartedAtBeforeOrEqual(any(LocalDate.class))).willReturn(
			priorityCoupons);

		// When
		CouponReadAllDto actual = couponService.readAll(authUser);

		// Then
		assertThat(actual.priorityCoupon().get(0).isFinished()).isTrue();
	}

	@DisplayName("아직 시작하지 않은 선착순 쿠폰을 조회한다. - findAllWithStartedAtBeforeOrEqual")
	@Test
	void readAll_findAllWithStartedAtBeforeOrEqual_empty() {
		// Given
		AuthUser authUser = MemberFixture.createAuthUserEntity();
		LocalDate nowDate = LocalDate.now().plusDays(2);

		given(clockHolder.nowDate()).willReturn(nowDate);
		given(priorityCouponRepository.findAllWithStartedAtBeforeOrEqual(nowDate)).willReturn(Collections.emptyList());

		// When
		CouponReadAllDto actual = couponService.readAll(authUser);

		// Then
		assertThat(actual.priorityCoupon()).isEmpty();
	}
}
