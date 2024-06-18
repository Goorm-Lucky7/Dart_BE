package com.dart.api.domain.coupon.entity;

import java.time.LocalDateTime;

import com.dart.global.common.entity.BaseTimeEntity;

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
@Table(name = "tbl_coupon")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon extends BaseTimeEntity {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "stock", nullable = false)
	private int stock;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "description", nullable = false)
	private String description;

	@Column(name = "valid_at", updatable = false, nullable = false)
	private LocalDateTime validAt;

	@Column(name = "duration_at", updatable = false, nullable = false)
	private LocalDateTime durationAt;

	@Column(name = "coupon_type")
	@Enumerated(EnumType.STRING)
	private CouponType couponType;

	@Builder
	public Coupon(
		int stock,
		String name,
		String description,
		LocalDateTime validAt,
		LocalDateTime durationAt,
		CouponType couponType
	) {
		this.stock = stock;
		this.name = name;
		this.description = description;
		this.validAt = validAt;
		this.durationAt = durationAt;
		this.couponType = couponType;
	}
}
