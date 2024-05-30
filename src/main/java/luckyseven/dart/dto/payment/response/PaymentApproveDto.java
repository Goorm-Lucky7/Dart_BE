package luckyseven.dart.dto.payment.response;

public record PaymentApproveDto(
	String item_code,
	AmountDto amount
) {
}
