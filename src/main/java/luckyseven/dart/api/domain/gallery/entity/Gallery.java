package luckyseven.dart.api.domain.gallery.entity;

import java.time.LocalDateTime;

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
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import luckyseven.dart.api.domain.member.entity.Member;
import luckyseven.dart.global.common.entity.BaseTimeEntity;

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

	@Column(name = "start_date", nullable = false)
	private LocalDateTime startDate;

	@Column(name = "end_date", nullable = false)
	private LocalDateTime endDate;

	@Column(name = "fee", nullable = false)
	private int fee;

	@Column(name = "review_average")
	private float reviewAverage;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private Member member;

	@Builder
	private Gallery(
		String title,
		String content,
		LocalDateTime startDate,
		LocalDateTime endDate,
		int fee,
		float reviewAverage
	) {
		this.title = title;
		this.content = content;
		this.startDate = startDate;
		this.endDate = endDate;
	}
}
