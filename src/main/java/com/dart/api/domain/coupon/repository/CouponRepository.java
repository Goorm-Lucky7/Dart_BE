package com.dart.api.domain.coupon.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.dart.api.domain.coupon.entity.Coupon;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {
	@Query("SELECT c FROM Coupon c WHERE c.id = :couponId AND c.createdAt <= :nowDate AND c.durationAt >= :nowDate")
	Optional<Coupon> findCouponByIdAndDateRange(Long couponId, LocalDateTime nowDate);

	@Query("SELECT c FROM Coupon c WHERE c.createdAt <= :nowDate AND c.durationAt >= :nowDate")
	Optional<Coupon> findCouponByDateRange(LocalDateTime nowDate);
}
