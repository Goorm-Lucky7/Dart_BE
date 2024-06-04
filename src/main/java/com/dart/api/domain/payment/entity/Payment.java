package com.dart.api.domain.payment.entity;

import java.time.LocalDateTime;

import com.dart.api.domain.gallery.entity.Gallery;
import com.dart.api.domain.member.entity.Member;
import com.dart.api.dto.payment.response.PaymentReadDto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "tbl_payment_info")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "amount")
	private int amount;

	@Column(name = "approved_at")
	private LocalDateTime approvedAt;

	@Column(name = "`order`")
	@Enumerated(EnumType.STRING)
	private Order order;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private Member member;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "gallery_id")
	private Gallery gallery;

	@Builder
	private Payment(
		int amount,
		LocalDateTime approvedAt,
		Member member,
		Gallery gallery,
		Order order
	) {
		this.amount = amount;
		this.approvedAt = approvedAt;
		this.member = member;
		this.gallery = gallery;
		this.order = order;
	}

	public static Payment create(Member member, Gallery gallery, LocalDateTime approvedAt, String order) {
		return Payment.builder()
			.member(member)
			.gallery(gallery)
			.approvedAt(approvedAt)
			.order(Order.fromValue(order))
			.amount(gallery.getFee())
			.build();
	}

	public PaymentReadDto toReadDto() {
		return PaymentReadDto.builder()
			.paymentId(this.id)
			.amount(this.amount)
			.approvedAt(this.approvedAt)
			.order(this.order.getValue())
			.galleryName(this.gallery.getTitle())
			.build();
	}
}
