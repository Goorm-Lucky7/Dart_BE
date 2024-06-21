package com.dart.api.domain.coupon.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dart.api.domain.coupon.entity.CouponEventType;
import com.dart.api.domain.coupon.entity.GeneralCoupon;
import com.dart.api.domain.coupon.entity.GeneralCouponWallet;
import com.dart.api.domain.member.entity.Member;

@Repository
public interface GeneralCouponWalletRepository extends JpaRepository<GeneralCouponWallet, Long> {
	boolean existsByGeneralCouponAndMember(GeneralCoupon generalCoupon, Member member);

	List<GeneralCouponWallet> findByGeneralCoupon_CouponEventType(CouponEventType couponEventType);
}
