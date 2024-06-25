package com.dart.api.domain.coupon.entity;

import java.time.LocalDate;

import com.dart.api.dto.coupon.response.PriorityCouponDetail;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "tbl_priority_coupon")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PriorityCoupon {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "stock", nullable = false)
	private int stock;

	@Column(name = "title", nullable = false)
	private String title;

	@Column(name = "started_at", updatable = false, nullable = false)
	private LocalDate startedAt;

	@Column(name = "ended_at", updatable = false, nullable = false)
	private LocalDate endedAt;

	@Column(name = "coupon_type")
	@Enumerated(EnumType.STRING)
	private CouponType couponType;

	@Builder
	public PriorityCoupon(
		int stock,
		String title,
		LocalDate startedAt,
		LocalDate endedAt,
		CouponType couponType
	) {
		this.stock = stock;
		this.title = title;
		this.startedAt = startedAt;
		this.endedAt = endedAt;
		this.couponType = couponType;
	}

	public PriorityCouponDetail toDetail(boolean isFinished) {
		return PriorityCouponDetail.builder()
			.priorityCouponId(this.id)
			.stock(this.stock)
			.startDate(this.startedAt)
			.endDate(this.endedAt)
			.title(this.title)
			.couponType(this.couponType.getValue())
			.isFinished(isFinished)
			.build();
	}
}
