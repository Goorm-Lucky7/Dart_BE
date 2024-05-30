package luckyseven.dart.dto.payment.response;

import java.util.Date;

public record PaymentReadyDto(
	String tid,
	String next_redirect_pc_url,
	Date created_at
) {
}
