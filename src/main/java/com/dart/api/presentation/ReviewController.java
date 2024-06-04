package com.dart.api.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.dart.api.application.review.ReviewService;
import com.dart.api.dto.review.request.ReviewCreateDto;
import com.dart.api.dto.review.response.PageResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController {
	private final ReviewService reviewService;

	@PostMapping
	public ResponseEntity<String> create(@RequestBody @Valid ReviewCreateDto dto) {
		reviewService.create(dto);

		return ResponseEntity.ok("OK");
	}

	@GetMapping("/{gallery-id}")
	public PageResponse readAll(
		@PathVariable(name = "gallery-id") Long galleryId,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size
	) {
		return reviewService.readAll(galleryId, page, size);
	}
}
