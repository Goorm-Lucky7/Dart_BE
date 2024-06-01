package luckyseven.dart.dto.review.response;

import java.util.List;

public record PageResponse(
	List<ReviewReadDto> reviews,
	PageInfo pageInfo
) {
}
