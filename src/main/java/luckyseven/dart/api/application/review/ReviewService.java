package luckyseven.dart.api.application.review;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import luckyseven.dart.api.domain.gallery.entity.Gallery;
import luckyseven.dart.api.domain.gallery.repo.GalleryRepository;
import luckyseven.dart.api.domain.review.entity.Review;
import luckyseven.dart.api.domain.review.repo.ReviewRepository;
import luckyseven.dart.dto.review.request.ReviewCreateDto;
import luckyseven.dart.global.error.exception.NotFoundException;
import luckyseven.dart.global.error.model.ErrorCode;

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
}
