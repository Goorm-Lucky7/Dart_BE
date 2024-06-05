package com.dart.api.dto.page;

import java.util.List;

public record PageResponse<T>(
	List<T> pages,
	PageInfo pageInfo
) {
}
