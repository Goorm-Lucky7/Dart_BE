package com.dart.api.domain.coupon.entity;

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

	@Column(name = "count", nullable = false)
	private int count;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "description", nullable = false)
	private String description;

	@Column(name = "coupon_type")
	@Enumerated(EnumType.STRING)
	private CouponType couponType;

	@Builder
	private Coupon(
		int stock,
		int count,
		String name,
		String description,
		CouponType couponType
	) {
		this.stock = stock;
		this.count = count;
		this.name = name;
		this.description = description;
		this.couponType = couponType;
	}

	public static Coupon create(
		int stock,
		int count,
		String name,
		String description,
		String couponType
	) {
		return Coupon.builder()
			.stock(stock)
			.count(count)
			.name(name)
			.description(description)
			.couponType(CouponType.fromName(couponType))
			.build();
	}
}
