package com.dart.presentation;

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
import com.dart.api.application.payment.PaymentService;
import com.dart.dto.payment.request.PaymentCreateDto;
import com.dart.dto.payment.response.PaymentApproveDto;
import com.dart.dto.payment.response.PaymentReadyDto;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payment")
public class PaymentController {
	private final PaymentService paymentService;

	@PostMapping
	public PaymentReadyDto ready(@RequestBody @Valid PaymentCreateDto dto) {
		return paymentService.ready(dto);
	}

	@GetMapping("/success/{id}/{order}")
	public PaymentApproveDto approve(
		@RequestParam("pg_token") String token,
		@PathVariable("id") Long id,
		@PathVariable("order") String order
	) {
		return paymentService.approve(token, id, order);
	}

	@GetMapping("/cancel")
	public ResponseEntity<String> cancel() {
		return ResponseEntity.internalServerError().body("결제 취소");
	}

	@GetMapping("/fail")
	public ResponseEntity<String> fail() {
		return ResponseEntity.internalServerError().body("결제 실패");
	}
}
