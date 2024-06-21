package com.dart.api.application.coupon;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dart.api.domain.auth.entity.AuthUser;
import com.dart.api.domain.coupon.entity.GeneralCoupon;
import com.dart.api.domain.coupon.entity.GeneralCouponWallet;
import com.dart.api.domain.coupon.repository.GeneralCouponRepository;
import com.dart.api.domain.coupon.repository.GeneralCouponWalletRepository;
import com.dart.api.domain.member.entity.Member;
import com.dart.api.domain.member.repository.MemberRepository;
import com.dart.api.dto.coupon.request.GeneralCouponPublishDto;
import com.dart.global.error.exception.BadRequestException;
import com.dart.global.error.exception.NotFoundException;
import com.dart.global.error.model.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class GeneralCouponManageService {
	private final GeneralCouponRepository generalCouponRepository;
	private final GeneralCouponWalletRepository generalCouponWalletRepository;
	private final MemberRepository memberRepository;

	public void publish(GeneralCouponPublishDto dto, AuthUser authUser) {
		final GeneralCoupon generalCoupon = generalCouponRepository.findById(dto.generalCouponId())
			.orElseThrow(() -> new NotFoundException(ErrorCode.FAIL_MEMBER_NOT_FOUND));
		final Member member = memberRepository.findByEmail(authUser.email())
			.orElseThrow(() -> new NotFoundException(ErrorCode.FAIL_MEMBER_NOT_FOUND));

		validateAlreadyCoupon(generalCoupon, member);

		final GeneralCouponWallet generalCouponWallet = GeneralCouponWallet.create(generalCoupon, member);
		generalCouponWalletRepository.save(generalCouponWallet);
	}

	private void validateAlreadyCoupon(GeneralCoupon generalCoupon, Member member) {
		if (generalCouponWalletRepository.existsByGeneralCouponAndMember(generalCoupon, member)) {
			throw new BadRequestException(ErrorCode.FAIL_MEMBER_NOT_FOUND);
		}
	}
}
