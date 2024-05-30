package luckyseven.dart.dto.payment.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PaymentCreateDto(

	@NotNull
	Long galleryId,
	@NotBlank(message = "주문 정보는 필수로 입력해 주세요.")
	String order
) {
}
