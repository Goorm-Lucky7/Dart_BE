package com.dart.api.domain.member.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.ColumnDefault;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.dart.api.domain.member.entity.Member;
import com.dart.api.domain.gallery.entity.Gallery;

@Entity
@Getter
@Table(name = "tbl_visitor")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Visitor {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "is_logged_in", nullable = false)
	@ColumnDefault("false")
	private boolean isLoggedIn;

	@Column(name="visit_time", nullable = false)
	private LocalDateTime visitTime;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "gallery_id")
	private Gallery gallery;

	@OneToOne(fetch = FetchType.LAZY, orphanRemoval = true)
	@JoinColumn(name = "member_id")
	private Member member;

	@Builder
	private Visitor(
		boolean isLoggedIn,
		LocalDateTime visitTime
	) {
		this.isLoggedIn = isLoggedIn;
		this.visitTime = visitTime;
	}
}
