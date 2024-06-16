package com.dart.api.domain.coupon.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dart.api.domain.coupon.entity.CouponWallet;

@Repository
public interface CouponWalletRepository extends JpaRepository<CouponWallet, Long> {
}
