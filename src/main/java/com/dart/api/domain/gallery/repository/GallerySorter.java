package com.dart.api.domain.gallery.repository;

import static com.dart.global.common.util.GlobalConstant.*;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.dart.api.domain.gallery.entity.Gallery;
import com.dart.api.domain.gallery.entity.QGallery;
import com.dart.api.domain.gallery.entity.Sort;
import com.dart.api.domain.review.entity.QReview;
import com.dart.global.common.util.ScoreUtil;
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
		LocalDateTime currentDateTime = LocalDateTime.now();

		if (sort == null || sort.isEmpty()) {
			orderByLatest(query, gallery, currentDateTime);
		} else {
			Sort sortEnum = Sort.fromValue(sort);
			switch (sortEnum) {
				case LATEST -> orderByLatest(query, gallery, currentDateTime);
				case LIKED -> orderByLiked(query, gallery, review, currentDateTime);
				default -> throw new BadRequestException(ErrorCode.FAIL_INVALID_SORT_VALUE);
			}
		}
	}

	private void orderByLatest(JPAQuery<Gallery> query, QGallery gallery, LocalDateTime currentDateTime) {
		NumberExpression<Integer> sortingOrder = getSortingOrder(gallery, currentDateTime);

		query.orderBy(sortingOrder.asc(), gallery.createdAt.desc());
	}

	private void orderByLiked(JPAQuery<Gallery> query, QGallery gallery, QReview review,
		LocalDateTime currentDateTime) {
		NumberExpression<Double> averageScore = ScoreUtil.getAverageScore(review);

		NumberExpression<Integer> sortingOrder = getSortingOrder(gallery, currentDateTime);

		query.leftJoin(review).on(review.gallery.eq(gallery))
			.groupBy(gallery.id)
			.orderBy(sortingOrder.asc(), averageScore.desc(), gallery.createdAt.desc());
	}

	private NumberExpression<Integer> getSortingOrder(QGallery gallery, LocalDateTime currentDateTime) {
		return new CaseBuilder()
			.when(gallery.endDate.isNull().or(gallery.endDate.after(currentDateTime)))
			.then(FIRST_SORT)
			.otherwise(SECOND_SORT);
	}
}
