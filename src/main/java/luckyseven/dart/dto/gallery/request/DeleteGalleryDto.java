package luckyseven.dart.dto.gallery.request;

import jakarta.validation.constraints.NotNull;

public record DeleteGalleryDto(
	@NotNull(message = "[❎ ERROR] 삭제하고자 하는 전시의 아이디 값을 입력해주세요.")
	Long galleryId
) {
}
