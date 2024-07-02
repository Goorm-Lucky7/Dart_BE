package com.dart.api.domain.payment.entity;

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
@Table(name = "tbl_order")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "tid", updatable = false, nullable = false)
	private String tid;

	@Column(name = "member_id", updatable = false, nullable = false)
	private Long memberId;

	@Column(name = "gallery_id", updatable = false, nullable = false)
	private Long galleryId;

	private Order(String tid, Long memberId, Long galleryId) {
		this.tid = tid;
		this.memberId = memberId;
		this.galleryId = galleryId;
	}

	public static Order create(String tid, Long memberId, Long galleryId) {
		return new Order(tid, memberId, galleryId);
	}
}
