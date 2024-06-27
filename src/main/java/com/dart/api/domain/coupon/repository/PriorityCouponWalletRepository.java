package com.dart.api.domain.coupon.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dart.api.domain.coupon.entity.PriorityCouponWallet;
import com.dart.api.domain.member.entity.Member;

@Repository
public interface PriorityCouponWalletRepository extends JpaRepository<PriorityCouponWallet, Long> {
	List<PriorityCouponWallet> findByMemberAndIsUsedFalse(Member member);

	Optional<PriorityCouponWallet> findByMemberIdAndPriorityCouponIdAndIsUsedFalse(Long memberId,
		Long priorityCouponId);
}
