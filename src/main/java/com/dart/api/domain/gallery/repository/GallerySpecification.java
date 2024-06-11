package com.dart.api.domain.gallery.repository;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.dart.api.domain.gallery.entity.Category;
import com.dart.api.domain.gallery.entity.Cost;
import com.dart.api.domain.gallery.entity.Display;
import com.dart.api.domain.gallery.entity.QGallery;
import com.dart.api.domain.gallery.entity.QHashtag;
import com.dart.global.error.exception.BadRequestException;
import com.dart.global.error.model.ErrorCode;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.JPAExpressions;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GallerySpecification {

	public BooleanBuilder applyIsPaidCondition() {
		final QGallery gallery = QGallery.gallery;
		return new BooleanBuilder(gallery.isPaid.isTrue());
	}

	public BooleanBuilder applyCostCondition(String cost) {
		final QGallery gallery = QGallery.gallery;
		BooleanBuilder builder = new BooleanBuilder();

		if (cost != null && !cost.isEmpty() && !"all".equals(cost)) {
			final Cost costEnum = Cost.fromValue(cost);
			if (costEnum != null) {
				builder.and(gallery.cost.eq(costEnum));
			} else {
				throw new BadRequestException(ErrorCode.FAIL_INVALID_COST_VALUE);
			}
		}

		return builder;
	}

	public BooleanBuilder applyDisplayCondition(String display) {
		final QGallery gallery = QGallery.gallery;
		BooleanBuilder builder = new BooleanBuilder();

		if (display != null && !display.isEmpty() && !"all".equals(display)) {
			final LocalDateTime now = LocalDateTime.now();
			final Display displayEnum = Display.fromValue(display);
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

		return builder;
	}

	public BooleanBuilder applySearchCondition(String category, String keyword) {
		final QGallery gallery = QGallery.gallery;
		BooleanBuilder builder = new BooleanBuilder();

		if (category != null && !category.isEmpty() && keyword != null && !keyword.isEmpty()) {
			Category categoryEnum;
			try {
				categoryEnum = Category.fromValue(category);
			} catch (IllegalArgumentException e) {
				throw new BadRequestException(ErrorCode.FAIL_INVALID_CATEGORY_VALUE);
			}

			switch (categoryEnum) {
				case HASHTAG -> {
					final QHashtag hashtag = QHashtag.hashtag;
					builder.and(gallery.in(
						JPAExpressions.select(hashtag.gallery)
							.from(hashtag)
							.where(hashtag.tag.containsIgnoreCase(keyword))
					));
				}
				case AUTHOR -> builder.and(gallery.member.nickname.containsIgnoreCase(keyword));
				case TITLE -> builder.and(gallery.title.containsIgnoreCase(keyword));
				default -> throw new BadRequestException(ErrorCode.FAIL_INVALID_CATEGORY_VALUE);
			}
		}

		return builder;
	}
}
