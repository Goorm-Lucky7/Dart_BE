package com.dart.api.domain.coupon.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.dart.api.domain.coupon.entity.PriorityCoupon;

@Repository
public interface PriorityCouponRepository extends JpaRepository<PriorityCoupon, Long> {
	@Query("SELECT pc "
		+ "FROM PriorityCoupon pc "
		+ "WHERE pc.id = :couponId "
		+ "AND pc.startedAt <= :nowDate "
		+ "AND pc.endedAt > :nowDate")
	Optional<PriorityCoupon> findCouponByIdAndDateRange(Long couponId, LocalDate nowDate);

	@Query("SELECT pc FROM PriorityCoupon pc WHERE pc.startedAt <= :nowDate AND pc.endedAt > :nowDate")
	Optional<PriorityCoupon> findCouponByDateRange(LocalDate nowDate);

	List<PriorityCoupon> findByStartedAt(LocalDate startedAt);
}
