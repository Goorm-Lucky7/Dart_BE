package com.dart.api.domain.payment.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dart.api.domain.payment.entity.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
	Optional<Order> findByMemberIdAndGalleryId(Long memberId, Long galleryId);

	boolean existsByMemberIdAndGalleryIdAndIsApprovedTrue(Long memberId, Long galleryId);
}
