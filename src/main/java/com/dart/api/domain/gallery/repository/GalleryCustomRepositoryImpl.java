package com.dart.api.domain.gallery.repository;

import static com.dart.global.common.util.GlobalConstant.*;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.dart.api.domain.gallery.entity.Gallery;
import com.dart.api.domain.gallery.entity.QGallery;
import com.dart.api.domain.review.entity.QReview;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class GalleryCustomRepositoryImpl implements GalleryCustomRepository {

	private final JPAQueryFactory queryFactory;
	private final GallerySpecification gallerySpecification;
	private final GallerySorter gallerySorter;

	@Override
	public Page<Gallery> findGalleriesByCriteria(Pageable pageable, String category, String keyword, String sort,
		String cost, String display) {
		final QGallery gallery = QGallery.gallery;
		final QReview review = QReview.review;

		BooleanBuilder builder = new BooleanBuilder()
			.and(gallerySpecification.applyIsPaidCondition())
			.and(gallerySpecification.applyCostCondition(cost))
			.and(gallerySpecification.applyDisplayCondition(display))
			.and(gallerySpecification.applySearchCondition(category, keyword));

		JPAQuery<Gallery> query = createQuery(builder, gallery, pageable, sort);

		List<Gallery> results = query.fetch();

		long totalCount = fetchTotalCount(builder, gallery);

		return new PageImpl<>(results, pageable, totalCount);
	}

	private JPAQuery<Gallery> createQuery(BooleanBuilder builder, QGallery gallery, Pageable pageable,
		String sort) {
		JPAQuery<Gallery> query = queryFactory.selectFrom(gallery)
			.leftJoin(gallery.member).fetchJoin()
			.where(builder)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize());

		gallerySorter.applySorting(query, sort);

		return query;
	}

	private long fetchTotalCount(BooleanBuilder builder, QGallery gallery) {
		Long total = queryFactory.select(gallery.count())
			.from(gallery)
			.where(builder)
			.fetchOne();

		if (total == null) {
			return DEFAULT_RESULT_COUNT;
		}

		return total;
	}
}
