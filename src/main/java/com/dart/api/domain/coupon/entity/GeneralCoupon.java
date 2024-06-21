package com.dart.api.domain.coupon.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

	private String title;

	private CouponType couponType;

	public GeneralCoupon(String title, CouponType couponType) {
		this.title = title;
		this.couponType = couponType;
	}
}
