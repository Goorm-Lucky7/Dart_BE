package com.dart.api.domain.coupon.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dart.api.domain.coupon.entity.PriorityCouponWallet;

@Repository
public interface PriorityCouponWalletRepository extends JpaRepository<PriorityCouponWallet, Long> {
}
