package com.dart.api.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dart.api.application.gallery.GalleryService;
import com.dart.api.application.review.ReviewService;
import com.dart.api.domain.auth.entity.AuthUser;
import com.dart.api.dto.gallery.response.ReviewGalleryInfoDto;
import com.dart.api.dto.page.PageResponse;
import com.dart.api.dto.review.request.ReviewCreateDto;
import com.dart.api.dto.review.response.ReviewReadDto;
import com.dart.global.auth.annotation.Auth;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController {
	private final ReviewService reviewService;
	private final GalleryService galleryService;

	@PostMapping
	public ResponseEntity<String> create(@RequestBody @Valid ReviewCreateDto dto, @Auth AuthUser authUser) {
		reviewService.create(dto, authUser);

		return ResponseEntity.ok("OK");
	}

	@GetMapping("/{gallery-id}")
	public PageResponse<ReviewReadDto> readAll(
		@PathVariable(name = "gallery-id") Long galleryId,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size) {
		return reviewService.readAll(galleryId, page, size);
	}

	@GetMapping("/info")
	public ResponseEntity<ReviewGalleryInfoDto> getReviewGalleryInfo(
		@RequestParam("gallery-id") Long galleryId) {
		return ResponseEntity.ok(galleryService.getReviewGalleryInfo(galleryId));
	}

}
