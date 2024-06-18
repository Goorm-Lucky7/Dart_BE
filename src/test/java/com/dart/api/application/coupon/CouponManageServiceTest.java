package com.dart.api.application.coupon;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dart.api.domain.coupon.entity.Coupon;
import com.dart.api.domain.coupon.entity.CouponWallet;
import com.dart.api.domain.coupon.repository.CouponRedisRepository;
import com.dart.api.domain.coupon.repository.CouponRepository;
import com.dart.api.domain.coupon.repository.CouponWalletRepository;
import com.dart.api.domain.member.entity.Member;
import com.dart.api.domain.member.repository.MemberRepository;
import com.dart.global.error.exception.BadRequestException;
import com.dart.global.error.exception.ConflictException;
import com.dart.support.CouponFixture;
import com.dart.support.MemberFixture;

@ExtendWith({MockitoExtension.class})
class CouponManageServiceTest {
	@Mock
	private CouponRepository couponRepository;

	@Mock
	private CouponRedisRepository couponRedisRepository;

	@Mock
	private CouponWalletRepository couponWalletRepository;

	@Mock
	private MemberRepository memberRepository;

	@InjectMocks
	private CouponManageService couponManageService;

	@DisplayName("10명의 사용자가 쿠폰 발행을 성공적으로 한다.")
	@MethodSource("com.dart.support.CouponFixture#provideValues_String")
	@ParameterizedTest
	void publish_all_success(Set<String> values) {
		// Given
		Coupon coupon = CouponFixture.create();
		given(couponRepository.findCouponByDateRange(any(LocalDateTime.class))).willReturn(Optional.of(coupon));
		given(couponRedisRepository.getCount(eq(coupon.getId()))).willReturn(coupon.getStock() - 1);
		given(couponRedisRepository.rangeQueue(eq(coupon.getId()), any(long.class), any(long.class)))
			.willReturn(values);

		values.forEach(email -> {
			Member mockMember = MemberFixture.createMemberEntity();
			given(memberRepository.findByEmail(email)).willReturn(Optional.of(mockMember));
		});

		// When
		couponManageService.publish();

		// Then
		verify(couponWalletRepository, times(10)).save(any(CouponWallet.class));
		verify(memberRepository, times(10)).findByEmail(any(String.class));
	}

	@DisplayName("현재 발행 가능한 쿠폰이 없다.")
	@Test
	void publish_not_durationAt() {
		// Given
		given(couponRepository.findCouponByDateRange(any(LocalDateTime.class))).willReturn(Optional.empty());

		// When
		couponManageService.publish();

		// Then
		verify(couponWalletRepository, times(0)).save(any(CouponWallet.class));
		verify(couponRedisRepository, times(0)).getCount(any(Long.class));
		verify(couponRedisRepository, times(0)).increase(any(Long.class), any(long.class));
		verify(couponRedisRepository, times(0))
			.rangeQueue(any(Long.class), any(long.class), any(long.class));
	}

	@Test
	@DisplayName("쿠폰 발급 요청을 성공적으로 대기열 큐에 등록한다. - Void")
	void registerQueue_Success() {
		// GIVEN
		Coupon coupon = CouponFixture.create();
		String testEmail = "test@example.com";

		given(couponRepository.findCouponByIdAndDateRange(eq(coupon.getId()), any(LocalDateTime.class))).willReturn(
			Optional.of(coupon));
		given(couponRedisRepository.hasValue(eq(coupon.getId()), eq(testEmail))).willReturn(false);
		given(couponRedisRepository.sizeQueue(eq(coupon.getId()))).willReturn(coupon.getStock() - 1);

		// WHEN
		couponManageService.registerQueue(coupon.getId(), testEmail);

		// THEN
		verify(couponRedisRepository)
			.addIfAbsentQueue(eq(coupon.getId()), eq(testEmail), anyDouble(), anyLong());
	}

	@Test
	@DisplayName("이미 해당 쿠폰은 발급받은 쿠폰이다. - ConflictException")
	void registerQueue_No_ConflictException() {
		// GIVEN
		Coupon coupon = CouponFixture.create();
		String testEmail = "test@example.com";

		given(couponRepository.findCouponByIdAndDateRange(eq(coupon.getId()), any(LocalDateTime.class))).willReturn(
			Optional.of(coupon));
		given(couponRedisRepository.hasValue(eq(coupon.getId()), eq(testEmail))).willReturn(true);

		// When & Then
		assertThatThrownBy(() -> couponManageService.registerQueue(coupon.getId(), testEmail))
			.isInstanceOf(ConflictException.class);
	}

	@Test
	@DisplayName("해당 쿠폰은 재고가 마감된 쿠폰이다. - BadRequestException")
	void registerQueue_No_BadRequestException() {
		// GIVEN
		Coupon coupon = CouponFixture.create();
		String testEmail = "test@example.com";

		given(couponRepository.findCouponByIdAndDateRange(eq(coupon.getId()), any(LocalDateTime.class))).willReturn(
			Optional.of(coupon));
		given(couponRedisRepository.hasValue(eq(coupon.getId()), eq(testEmail))).willReturn(false);
		given(couponRedisRepository.sizeQueue(eq(coupon.getId()))).willReturn(coupon.getStock());

		// When & Then
		assertThatThrownBy(() -> couponManageService.registerQueue(coupon.getId(), testEmail))
			.isInstanceOf(BadRequestException.class);
	}
}

