package com.dart.api.domain.gallery.entity;

import java.time.LocalDateTime;

import com.dart.api.domain.member.entity.Member;
import com.dart.api.dto.gallery.request.CreateGalleryDto;
import com.dart.api.dto.gallery.response.GalleryReadIdDto;
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

	@Enumerated(EnumType.STRING)
	@Column(name = "template", nullable = false)
	private Template template;

	@Column(name = "fee", nullable = false)
	private int fee;

	@Column(name = "generated_cost", nullable = false)
	private int generatedCost;

	@Column(name = "is_paid")
	private boolean isPaid;

	@Column(name = "re_exhibition_request_count", nullable = false)
	private int reExhibitionRequestCount;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "meber_id")
	private Member member;

	@Builder
	public Gallery(String title, String content, String thumbnail, LocalDateTime startDate, LocalDateTime endDate,
		Cost cost, Template template, int fee, int generatedCost, Member member) {
		this.title = title;
		this.content = content;
		this.thumbnail = thumbnail;
		this.startDate = startDate;
		this.cost = cost;
		this.template = template;
		this.endDate = endDate;
		this.fee = fee;
		this.generatedCost = generatedCost;
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
			.template(Template.fromValue(createGalleryDto.template()))
			.fee(createGalleryDto.fee())
			.generatedCost(createGalleryDto.generatedCost())
			.thumbnail(thumbnailUrl)
			.member(member)
			.build();
	}

	public boolean isMine(Member member) {
		return this.member.equals(member);
	}

	public void pay() {
		this.isPaid = true;
	}

	public GalleryReadIdDto toReadIdDto() {
		return new GalleryReadIdDto(this.id);
	}

	public void incrementReExhibitionRequestCount() {
		this.reExhibitionRequestCount++;
	}

	public void resetReExhibitionRequestCount() {
		this.reExhibitionRequestCount = 0;
	}
}
