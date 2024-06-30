package com.dart.api.domain.coupon.entity;

import com.dart.api.dto.coupon.response.MyCouponDetail;
import com.dart.global.common.entity.BaseTimeEntity;

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
@Table(name = "tbl_priority_coupon_wallet")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PriorityCouponWallet extends BaseTimeEntity {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "member_id", updatable = false, nullable = false)
	private Long memberId;

	@Column(name = "is_used")
	private boolean isUsed;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "priority_coupon_id")
	private PriorityCoupon priorityCoupon;

	public PriorityCouponWallet(PriorityCoupon priorityCoupon, Long memberId) {
		this.priorityCoupon = priorityCoupon;
		this.memberId = memberId;
		this.isUsed = false;
	}

	public static PriorityCouponWallet create(PriorityCoupon priorityCoupon, Long memberId) {
		return new PriorityCouponWallet(priorityCoupon, memberId);
	}

	public MyCouponDetail toDetail() {
		return MyCouponDetail.builder()
			.couponId(this.priorityCoupon.getId())
			.title(this.priorityCoupon.getTitle())
			.couponType(this.priorityCoupon.getCouponType().getValue())
			.isPriority(true)
			.build();
	}

	public void use() {
		this.isUsed = true;
	}
}
