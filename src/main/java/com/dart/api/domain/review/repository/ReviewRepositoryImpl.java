package com.dart.api.domain.review.repository;

import java.util.Optional;

import com.dart.api.domain.review.entity.QReview;
import com.dart.global.common.util.ScoreUtil;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ReviewRepositoryImpl implements ReviewRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public Float calculateAverageScoreByGalleryId(Long galleryId) {
		QReview review = QReview.review;

		NumberExpression<Double> averageScore = ScoreUtil.getAverageScore(review);

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
