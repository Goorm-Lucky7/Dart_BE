package com.dart.api.domain.gallery.repository;

import static com.dart.global.common.util.GlobalConstant.*;

import org.springframework.stereotype.Component;

import com.dart.api.domain.gallery.entity.Gallery;
import com.dart.api.domain.gallery.entity.QGallery;
import com.dart.api.domain.gallery.entity.Sort;
import com.dart.api.domain.review.entity.QReview;
import com.dart.api.domain.review.entity.Score;
import com.dart.global.error.exception.BadRequestException;
import com.dart.global.error.model.ErrorCode;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQuery;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GallerySorter {

	public void applySorting(JPAQuery<Gallery> query, String sort) {
		final QGallery gallery = QGallery.gallery;
		final QReview review = QReview.review;

		if (sort == null || sort.isEmpty()) {
			orderByLatest(query, gallery);
		} else {
			Sort sortEnum = Sort.fromValue(sort);
			switch (sortEnum) {
				case LATEST -> orderByLatest(query, gallery);
				case LIKED -> orderByLiked(query, gallery, review);
				default -> throw new BadRequestException(ErrorCode.FAIL_INVALID_SORT_VALUE);
			}
		}
	}

	private void orderByLatest(JPAQuery<Gallery> query, QGallery gallery) {
		query.orderBy(gallery.createdAt.desc());
	}

	private void orderByLiked(JPAQuery<Gallery> query, QGallery gallery, QReview review) {
		NumberExpression<Double> averageScore = new CaseBuilder()
			.when(review.score.eq(Score.ONE_STAR)).then(ONE_STAR)
			.when(review.score.eq(Score.TWO_STAR)).then(TWO_STAR)
			.when(review.score.eq(Score.THREE_STAR)).then(THREE_STAR)
			.when(review.score.eq(Score.FOUR_STAR)).then(FOUR_STAR)
			.when(review.score.eq(Score.FIVE_STAR)).then(FIVE_STAR)
			.otherwise(ZERO_STAR)
			.avg();

		query.leftJoin(review).on(review.gallery.eq(gallery))
			.groupBy(gallery.id)
			.orderBy(averageScore.desc(), gallery.createdAt.desc());
	}
}
