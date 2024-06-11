package com.dart.api.domain.payment.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dart.api.domain.gallery.entity.Gallery;
import com.dart.api.domain.member.entity.Member;
import com.dart.api.domain.payment.entity.Order;
import com.dart.api.domain.payment.entity.Payment;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
	//결제정보 최근 승인일 순
	Page<Payment> findAllByMemberOrderByApprovedAtDesc(Member member, Pageable pageable);

	boolean existsByMemberAndGalleryAndOrder(Member member, Gallery gallery, Order order);

	boolean existsByMemberAndGalleryIdAndOrder(Member member, Long galleryId, Order order);
}
