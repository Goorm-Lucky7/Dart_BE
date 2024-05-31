package luckyseven.dart.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import luckyseven.dart.api.application.review.ReviewService;
import luckyseven.dart.dto.review.request.ReviewCreateDto;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController {
	private final ReviewService reviewService;

	@PostMapping
	public ResponseEntity<String> create(ReviewCreateDto dto) {
		reviewService.create(dto);

		return ResponseEntity.ok("OK");
	}
}
