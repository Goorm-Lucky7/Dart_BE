package luckyseven.dart.dto.review.response;

import java.time.LocalDateTime;

import lombok.Builder;

@Builder
public record ReviewReadDto(
	Long reviewId,
	LocalDateTime createAt,
	String content,
	int score,
	String nickname,
	String profileImage
) {
}
