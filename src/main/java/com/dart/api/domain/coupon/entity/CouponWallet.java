package com.dart.api.domain.coupon.entity;

import com.dart.api.domain.member.entity.Member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "tbl_coupon_wallet")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CouponWallet {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "coupon_id")
	private Coupon coupon;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private Member member;

	private CouponWallet(Coupon coupon, Member member) {
		this.coupon = coupon;
		this.member = member;
	}

	public static CouponWallet create(Coupon coupon, Member member) {
		return new CouponWallet(coupon, member);
	}
}
