package com.dart.api.domain.gallery.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.dart.api.domain.gallery.entity.Category;
import com.dart.api.domain.gallery.entity.Cost;
import com.dart.api.domain.gallery.entity.Display;
import com.dart.api.domain.gallery.entity.Gallery;
import com.dart.api.domain.gallery.entity.QGallery;
import com.dart.api.domain.gallery.entity.QHashtag;
import com.dart.api.domain.gallery.entity.Sort;
import com.dart.api.domain.review.entity.QReview;
import com.dart.api.domain.review.entity.Score;
import com.dart.global.error.exception.BadRequestException;
import com.dart.global.error.model.ErrorCode;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class GalleryCustomRepositoryImpl implements GalleryCustomRepository {

	private final JPAQueryFactory queryFactory;

	@Override
	public Page<Gallery> findGalleriesByCriteria(Pageable pageable, String category, String keyword, String sort,
		String cost, String display) {
		QGallery gallery = QGallery.gallery;
		QReview review = QReview.review;

		BooleanBuilder builder = new BooleanBuilder();

		applyIsPaidCondition(builder, gallery);
		applyCostCondition(builder, gallery, cost);
		applyDisplayCondition(builder, gallery, display);
		applySearchCondition(builder, gallery, category, keyword);

		JPAQuery<Gallery> query = createQuery(builder, gallery, review, pageable, sort);

		List<Gallery> results = query.fetch();

		long totalCount = fetchTotalCount(builder, gallery);

		return new PageImpl<>(results, pageable, totalCount);
	}

	private void applyIsPaidCondition(BooleanBuilder builder, QGallery gallery) {
		builder.and(gallery.isPaid.isTrue());
	}

	private void applyCostCondition(BooleanBuilder builder, QGallery gallery, String cost) {
		if (cost != null && !cost.isEmpty() && !"all".equals(cost)) {
			Cost costEnum = Cost.fromValue(cost);
			if (costEnum != null) {
				builder.and(gallery.cost.eq(costEnum));
			} else {
				throw new BadRequestException(ErrorCode.FAIL_INVALID_COST_VALUE);
			}
		}
	}

	private void applyDisplayCondition(BooleanBuilder builder, QGallery gallery, String display) {
		if (display != null && !display.isEmpty() && !"all".equals(display)) {
			LocalDateTime now = LocalDateTime.now();
			Display displayEnum = Display.fromValue(display);
			if (displayEnum != null) {
				switch (displayEnum) {
					case UPCOMING -> builder.and(gallery.startDate.gt(now));
					case INPROGRESS -> builder.and(
						gallery.startDate.loe(now)
							.and(gallery.endDate.isNull().or(gallery.endDate.goe(now)))
					);
					case FINISHED -> builder.and(gallery.endDate.lt(now));
					default -> throw new BadRequestException(ErrorCode.FAIL_INVALID_DISPLAY_VALUE);
				}
			} else {
				throw new BadRequestException(ErrorCode.FAIL_INVALID_DISPLAY_VALUE);
			}
		}
	}

	private void applySearchCondition(BooleanBuilder builder, QGallery gallery, String category, String keyword) {
		if (category != null && !category.isEmpty() && keyword != null && !keyword.isEmpty()) {
			Category categoryEnum;
			try {
				categoryEnum = Category.fromValue(category);
			} catch (IllegalArgumentException e) {
				throw new BadRequestException(ErrorCode.FAIL_INVALID_CATEGORY_VALUE);
			}

			switch (categoryEnum) {
				case HASHTAG -> {
					QHashtag hashtag = QHashtag.hashtag;
					builder.and(gallery.in(
						JPAExpressions.select(hashtag.gallery)
							.from(hashtag)
							.where(hashtag.tag.containsIgnoreCase(keyword))
					));
				}
				case AUTHOR -> {
					builder.and(gallery.member.nickname.containsIgnoreCase(keyword));
				}
				case TITLE -> builder.and(gallery.title.containsIgnoreCase(keyword));
				default -> throw new BadRequestException(ErrorCode.FAIL_INVALID_CATEGORY_VALUE);
			}
		}
	}

	private void applySorting(JPAQuery<Gallery> query, QGallery gallery, QReview review, String sort) {
		if (sort == null || sort.isEmpty()) {
			return;
		}

		Sort sortEnum = Sort.fromValue(sort);
		switch (sortEnum) {
			case LATEST -> query.orderBy(gallery.createdAt.desc());
			case LIKED -> {
				// 별점을 숫자로 변환하여 평균 계산
				NumberExpression<Double> averageScore = new CaseBuilder()
					.when(review.score.eq(Score.ONE_STAR)).then(1)
					.when(review.score.eq(Score.TWO_STAR)).then(2)
					.when(review.score.eq(Score.THREE_STAR)).then(3)
					.when(review.score.eq(Score.FOUR_STAR)).then(4)
					.when(review.score.eq(Score.FIVE_STAR)).then(5)
					.otherwise(0)
					.avg();

				// 별점 평균으로 정렬하고, 최신순으로 정렬
				query.leftJoin(review).on(review.gallery.eq(gallery))
					.groupBy(gallery.id)
					.orderBy(averageScore.desc(), gallery.createdAt.desc());
			}
			default -> throw new BadRequestException(ErrorCode.FAIL_INVALID_SORT_VALUE);
		}
	}

	private JPAQuery<Gallery> createQuery(BooleanBuilder builder, QGallery gallery, QReview review, Pageable pageable,
		String sort) {
		JPAQuery<Gallery> query = queryFactory.selectFrom(gallery)
			.leftJoin(gallery.member).fetchJoin()
			.where(builder)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize());

		applySorting(query, gallery, review, sort);

		return query;
	}

	private long fetchTotalCount(BooleanBuilder builder, QGallery gallery) {
		Long total = queryFactory.select(gallery.count())
			.from(gallery)
			.where(builder)
			.fetchOne();

		if (total == null) {
			return 0L;
		}

		return total;
	}
}
