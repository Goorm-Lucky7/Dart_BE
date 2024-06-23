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
@Table(name = "tbl_general_coupon_wallet")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GeneralCouponWallet {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "general_coupon_id")
	private GeneralCoupon generalCoupon;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private Member member;

	public GeneralCouponWallet(GeneralCoupon generalCoupon, Member member) {
		this.generalCoupon = generalCoupon;
		this.member = member;
	}
}
