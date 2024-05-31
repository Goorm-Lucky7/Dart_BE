package luckyseven.dart.api.domain.review.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import luckyseven.dart.api.domain.gallery.entity.Gallery;
import luckyseven.dart.api.domain.member.entity.Member;
import luckyseven.dart.dto.review.request.ReviewCreateDto;
import luckyseven.dart.dto.review.response.ReviewReadDto;
import luckyseven.dart.global.common.BaseTimeEntity;

@Entity
@Getter
@Table(name = "tbl_review")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseTimeEntity {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "content", nullable = false)
	private String content;

	@Column(name = "score")
	private Score score;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "gallery_id")
	private Gallery gallery;

	@OneToOne(fetch = FetchType.LAZY, orphanRemoval = true)
	@JoinColumn(name = "member_id")
	private Member member;

	@Builder
	private Review(String content, Score score, Gallery gallery, Member member) {
		this.content = content;
		this.score = score;
		this.gallery = gallery;
		this.member = member;
	}

	public static Review create(ReviewCreateDto dto, Gallery gallery) {
		return Review.builder()
			.content(dto.content())
			.score(Score.fromValue(dto.score()))
			.gallery(gallery)
			.build();
	}

	public ReviewReadDto toReviewReadDto() {
		return ReviewReadDto.builder()
			.reviewId(this.id)
			.content(this.content)
			.score(this.score.getValue())
			.createAt(this.getCreatedAt())
			.nickname(this.member.getNickname())
			.build();
	}
}
