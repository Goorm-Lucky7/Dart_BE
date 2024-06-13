package com.dart.api.domain.coupon.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dart.api.domain.coupon.entity.CouponWallet;

public interface CouponWalletRepository extends JpaRepository<CouponWallet, Long> {
}
