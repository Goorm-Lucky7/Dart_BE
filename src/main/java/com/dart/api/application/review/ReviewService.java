package com.dart.api.application.review;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import com.dart.api.domain.gallery.entity.Gallery;
import com.dart.api.domain.gallery.repo.GalleryRepository;
import com.dart.api.domain.review.entity.Review;
import com.dart.api.domain.review.repo.ReviewRepository;
import com.dart.dto.review.request.ReviewCreateDto;
import com.dart.dto.review.response.PageInfo;
import com.dart.dto.review.response.PageResponse;
import com.dart.global.error.exception.NotFoundException;
import com.dart.global.error.model.ErrorCode;

@Transactional
@RestController
@RequiredArgsConstructor
public class ReviewService {
	private final ReviewRepository reviewRepository;
	private final GalleryRepository galleryRepository;

	public void create(ReviewCreateDto dto) {
		final Gallery gallery = galleryRepository.findById(dto.galleryId())
			.orElseThrow(() -> new NotFoundException(ErrorCode.FAIL_GALLERY_NOT_FOUND));
		final Review review = Review.create(dto, gallery);

		reviewRepository.save(review);
	}

	@Transactional(readOnly = true)
	public PageResponse readAll(Long galleryId, int page, int size) {
		final Gallery gallery = galleryRepository.findById(galleryId)
			.orElseThrow(() -> new NotFoundException(ErrorCode.FAIL_GALLERY_NOT_FOUND));
		final Pageable pageable = PageRequest.of(page, size);
		final Page<Review> reviews = reviewRepository.findAllByGalleryOrderByCreatedAtDesc(gallery, pageable);
		final PageInfo pageInfo = new PageInfo(reviews.getNumber(), reviews.isLast());

		return new PageResponse(reviews.map(Review::toReviewReadDto).toList(), pageInfo);
	}
}
