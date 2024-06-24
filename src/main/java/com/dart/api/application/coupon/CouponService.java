package com.dart.api.application.coupon;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import com.dart.global.error.exception.NotFoundException;
import com.dart.global.error.model.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CouponService {
	private final MemberRepository memberRepository;
	private final PriorityCouponRepository priorityCouponRepository;
	private final GeneralCouponRepository generalCouponRepository;
	private final ClockHolder clockHolder;
	private final GeneralCouponWalletRepository generalCouponWalletRepository;

	public CouponReadAllDto readAll(AuthUser authUser) {
		final LocalDate date = clockHolder.nowDate();
		final List<PriorityCoupon> priorityCoupons = priorityCouponRepository.findAll();
		final List<GeneralCoupon> generalCoupons = generalCouponRepository.findAll()
			.stream()
			.sorted((gc1, gc2) -> gc2.getCouponType().getValue() - gc1.getCouponType().getValue())
			.toList();

		return new CouponReadAllDto(
			priorityCoupons.stream()
				.map(priorityCoupon -> priorityCoupon.toDetail(isFinished(priorityCoupon.getEndedAt(), date)))
				.toList(),
			generalCoupons.stream()
				.map(generalCoupon -> generalCoupon.toDetail(isAlreadyCoupon(generalCoupon, authUser)))
				.toList()
		);
	}

	private boolean isFinished(LocalDate endedAt, LocalDate nowDate) {
		return endedAt.isAfter(nowDate);
	}

	private boolean isAlreadyCoupon(GeneralCoupon generalCoupon, AuthUser authUser) {
		if (authUser == null) {
			return false;
		}

		final Member member = memberRepository.findByEmail(authUser.email())
			.orElseThrow(() -> new NotFoundException(ErrorCode.FAIL_MEMBER_NOT_FOUND));

		return generalCouponWalletRepository.existsByGeneralCouponAndMember(generalCoupon, member);
	}
}
