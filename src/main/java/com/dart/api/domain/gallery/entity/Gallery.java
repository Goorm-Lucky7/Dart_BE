package com.dart.api.domain.gallery.entity;

import java.time.LocalDateTime;

import com.dart.api.domain.member.entity.Member;
import com.dart.dto.gallery.request.CreateGalleryDto;
import com.dart.global.common.entity.BaseTimeEntity;

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
@Table(name = "tbl_gallery")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Gallery extends BaseTimeEntity {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "title", nullable = false)
	private String title;

	@Column(name = "content", nullable = false)
	private String content;

	@Column(name = "thumbnail", nullable = false)
	private String thumbnail;

	@Column(name = "start_date", nullable = false)
	private LocalDateTime startDate;

	@Column(name = "end_date")
	private LocalDateTime endDate;

	@Enumerated(EnumType.STRING)
	@Column(name = "cost", nullable = false)
	private Cost cost;

	@Column(name = "fee", nullable = false)
	private int fee;

	@Column(name = "review_average")
	private float reviewAverage;

	@Column(name = "is_paid")
	private boolean isPaid;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "meber_id")
	private Member member;

	@Builder
	private Gallery(String title, String content, String thumbnail, LocalDateTime startDate, LocalDateTime endDate,
		Cost cost, int fee, float reviewAverage, Member member) {
		this.title = title;
		this.content = content;
		this.thumbnail = thumbnail;
		this.startDate = startDate;
		this.cost = cost;
		this.endDate = endDate;
		this.fee = fee;
		this.reviewAverage = reviewAverage;
		this.isPaid = !Cost.PAY.equals(cost);
		this.member = member;
	}

	public static Gallery create(CreateGalleryDto createGalleryDto, String thumbnailUrl, Cost cost, Member member) {
		return Gallery.builder()
			.title(createGalleryDto.title())
			.content(createGalleryDto.content())
			.startDate(createGalleryDto.startDate())
			.endDate(createGalleryDto.endDate())
			.cost(cost)
			.fee(createGalleryDto.fee())
			.reviewAverage(0.0f)
			.thumbnail(thumbnailUrl)
			.member(member)
			.build();
	}
}
