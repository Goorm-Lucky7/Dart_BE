package com.dart.dto.payment.response;

public record PaymentReadyDto(
	String tid,
	String next_redirect_pc_url
) {
}
