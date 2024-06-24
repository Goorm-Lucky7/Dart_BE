package com.dart.api.domain.coupon.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "tbl_general_coupon")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GeneralCoupon {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "title", nullable = false)
	private String title;

	@Column(name = "coupon_type")
	@Enumerated(EnumType.STRING)
	private CouponType couponType;

	@Column(name = "coupon_event_type")
	@Enumerated(EnumType.STRING)
	private CouponEventType couponEventType;

	public GeneralCoupon(String title, CouponType couponType, CouponEventType couponEventType) {
		this.title = title;
		this.couponType = couponType;
		this.couponEventType = couponEventType;
	}
}
