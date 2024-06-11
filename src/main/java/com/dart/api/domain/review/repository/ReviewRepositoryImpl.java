package com.dart.api.domain.review.repository;

import static com.dart.global.common.util.GlobalConstant.*;

import java.util.Optional;

import com.dart.api.domain.review.entity.QReview;
import com.dart.api.domain.review.entity.Score;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ReviewRepositoryImpl implements ReviewRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public Float calculateAverageScoreByGalleryId(Long galleryId) {
		QReview review = QReview.review;

		NumberExpression<Double> averageScore = new CaseBuilder()
			.when(review.score.eq(Score.ONE_STAR)).then(ONE_STAR)
			.when(review.score.eq(Score.TWO_STAR)).then(TWO_STAR)
			.when(review.score.eq(Score.THREE_STAR)).then(THREE_STAR)
			.when(review.score.eq(Score.FOUR_STAR)).then(FOUR_STAR)
			.when(review.score.eq(Score.FIVE_STAR)).then(FIVE_STAR)
			.otherwise(ZERO_STAR)
			.avg();

		Double result = queryFactory
			.select(averageScore)
			.from(review)
			.where(review.gallery.id.eq(galleryId))
			.fetchOne();

		return Optional.ofNullable(result)
			.map(r -> Math.round(r * 10) / 10.0f)
			.orElse(null);
	}
}
