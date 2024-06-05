package com.dart.api.application.review;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import com.dart.api.domain.auth.AuthUser;
import com.dart.api.domain.gallery.entity.Gallery;
import com.dart.api.domain.gallery.repository.GalleryRepository;
import com.dart.api.domain.member.entity.Member;
import com.dart.api.domain.member.repository.MemberRepository;
import com.dart.api.domain.review.entity.Review;
import com.dart.api.domain.review.repository.ReviewRepository;
import com.dart.api.dto.page.PageInfo;
import com.dart.api.dto.page.PageResponse;
import com.dart.api.dto.review.request.ReviewCreateDto;
import com.dart.api.dto.review.response.ReviewReadDto;
import com.dart.global.error.exception.NotFoundException;
import com.dart.global.error.model.ErrorCode;

import lombok.RequiredArgsConstructor;

@Transactional
@RestController
@RequiredArgsConstructor
public class ReviewService {
	private final ReviewRepository reviewRepository;
	private final GalleryRepository galleryRepository;
	private final MemberRepository memberRepository;

	public void create(ReviewCreateDto dto, AuthUser authUser) {
		final Member member = memberRepository.findByEmail(authUser.email())
			.orElseThrow(() -> new NotFoundException(ErrorCode.FAIL_MEMBER_NOT_FOUND));
		final Gallery gallery = galleryRepository.findById(dto.galleryId())
			.orElseThrow(() -> new NotFoundException(ErrorCode.FAIL_GALLERY_NOT_FOUND));
		final Review review = Review.create(dto, gallery, member);

		reviewRepository.save(review);
	}

	@Transactional(readOnly = true)
	public PageResponse<ReviewReadDto> readAll(Long galleryId, int page, int size) {
		final Gallery gallery = galleryRepository.findById(galleryId)
			.orElseThrow(() -> new NotFoundException(ErrorCode.FAIL_GALLERY_NOT_FOUND));
		final Pageable pageable = PageRequest.of(page, size);
		final Page<Review> reviews = reviewRepository.findAllByGalleryOrderByCreatedAtDesc(gallery, pageable);
		final PageInfo pageInfo = new PageInfo(reviews.getNumber(), reviews.isLast());

		return new PageResponse<>(reviews.map(Review::toReviewReadDto).toList(), pageInfo);
	}
}
