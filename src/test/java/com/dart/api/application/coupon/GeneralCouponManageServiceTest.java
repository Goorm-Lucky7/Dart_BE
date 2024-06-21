package com.dart.api.application.coupon;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.BDDMockito.*;

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
import com.dart.api.domain.coupon.repository.GeneralCouponRepository;
import com.dart.api.domain.coupon.repository.GeneralCouponWalletRepository;
import com.dart.api.domain.member.entity.Member;
import com.dart.api.domain.member.repository.MemberRepository;
import com.dart.api.dto.coupon.request.GeneralCouponPublishDto;
import com.dart.global.error.exception.ConflictException;
import com.dart.support.CouponFixture;
import com.dart.support.MemberFixture;

@ExtendWith({MockitoExtension.class})
class GeneralCouponManageServiceTest {
	@Mock
	private MemberRepository memberRepository;

	@Mock
	private GeneralCouponRepository generalCouponRepository;

	@Mock
	private GeneralCouponWalletRepository generalCouponWalletRepository;

	@InjectMocks
	private GeneralCouponManageService generalCouponManageService;

	@Test
	@DisplayName("일반 쿠폰을 성공적으로 발급한다. - Void")
	void publish_Success() {
		// GIVEN
		GeneralCoupon generalCoupon = CouponFixture.createGeneralCoupon();
		Member member = MemberFixture.createMemberEntity();
		AuthUser authUser = MemberFixture.createAuthUserEntity();
		GeneralCouponPublishDto dto = new GeneralCouponPublishDto(generalCoupon.getId());

		given(generalCouponRepository.findById(dto.generalCouponId())).willReturn(Optional.of(generalCoupon));
		given(memberRepository.findByEmail(authUser.email())).willReturn(Optional.ofNullable(member));
		given(generalCouponWalletRepository.existsByGeneralCouponAndMember(generalCoupon, member)).willReturn(false);

		// WHEN
		generalCouponManageService.publish(dto, authUser);

		// THEN
		verify(generalCouponWalletRepository, times(1)).save(any(GeneralCouponWallet.class));
	}

	@Test
	@DisplayName("이미 해당 쿠폰은 발급받은 쿠폰이다. - ConflictException")
	void publish_No_ConflictException() {
		// GIVEN
		GeneralCoupon generalCoupon = CouponFixture.createGeneralCoupon();
		Member member = MemberFixture.createMemberEntity();
		AuthUser authUser = MemberFixture.createAuthUserEntity();
		GeneralCouponPublishDto dto = new GeneralCouponPublishDto(generalCoupon.getId());

		given(generalCouponRepository.findById(dto.generalCouponId())).willReturn(Optional.of(generalCoupon));
		given(memberRepository.findByEmail(authUser.email())).willReturn(Optional.ofNullable(member));
		given(generalCouponWalletRepository.existsByGeneralCouponAndMember(generalCoupon, member)).willReturn(true);

		// WHEN & THEN
		assertThatThrownBy(() -> generalCouponManageService.publish(dto, authUser))
			.isInstanceOf(ConflictException.class);
	}
}
