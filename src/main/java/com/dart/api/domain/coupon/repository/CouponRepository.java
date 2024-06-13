package com.dart.api.domain.coupon.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dart.api.domain.coupon.entity.Coupon;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
}
