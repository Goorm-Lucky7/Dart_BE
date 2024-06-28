package com.dart.api.domain.gallery.repository;

import static com.dart.global.common.util.GlobalConstant.*;

import java.time.LocalDate;

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
		LocalDate currentDate = LocalDate.now();

		if (sort == null || sort.isEmpty()) {
			orderByLatest(query, gallery, currentDate);
		} else {
			Sort sortEnum = Sort.fromValue(sort);
			switch (sortEnum) {
				case LATEST -> orderByLatest(query, gallery, currentDate);
				case LIKED -> orderByLiked(query, gallery, review, currentDate);
				default -> throw new BadRequestException(ErrorCode.FAIL_INVALID_SORT_VALUE);
			}
		}
	}

	private void orderByLatest(JPAQuery<Gallery> query, QGallery gallery, LocalDate currentDate) {
		NumberExpression<Integer> sortingOrder = getSortingOrder(gallery, currentDate);

		query.orderBy(sortingOrder.asc(), gallery.createdAt.desc());
	}

	private void orderByLiked(JPAQuery<Gallery> query, QGallery gallery, QReview review, LocalDate currentDate) {
		NumberExpression<Double> averageScore = new CaseBuilder()
			.when(review.score.eq(Score.ONE_STAR)).then(ONE_STAR)
			.when(review.score.eq(Score.TWO_STAR)).then(TWO_STAR)
			.when(review.score.eq(Score.THREE_STAR)).then(THREE_STAR)
			.when(review.score.eq(Score.FOUR_STAR)).then(FOUR_STAR)
			.when(review.score.eq(Score.FIVE_STAR)).then(FIVE_STAR)
			.otherwise(ZERO_STAR)
			.avg();

		NumberExpression<Integer> sortingOrder = getSortingOrder(gallery, currentDate);

		query.leftJoin(review).on(review.gallery.eq(gallery))
			.groupBy(gallery.id)
			.orderBy(sortingOrder.asc(), averageScore.desc(), gallery.createdAt.desc());
	}

	private NumberExpression<Integer> getSortingOrder(QGallery gallery, LocalDate currentDate) {
		return new CaseBuilder()
			.when(gallery.endDate.isNull().or(gallery.endDate.after(currentDate.atStartOfDay())))
			.then(FIRST_SORT)
			.otherwise(SECOND_SORT);
	}
}
