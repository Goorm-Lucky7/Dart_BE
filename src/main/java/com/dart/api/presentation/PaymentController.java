package com.dart.api.presentation;

import static com.dart.global.common.util.PaymentConstant.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import com.dart.api.application.payment.PaymentService;
import com.dart.api.domain.auth.entity.AuthUser;
import com.dart.api.dto.page.PageResponse;
import com.dart.api.dto.payment.request.PaymentCreateDto;
import com.dart.api.dto.payment.response.PaymentReadDto;
import com.dart.api.dto.payment.response.PaymentReadyDto;
import com.dart.global.auth.annotation.Auth;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payment")
public class PaymentController {
	private final PaymentService paymentService;

	@PostMapping
	public PaymentReadyDto ready(@RequestBody @Valid PaymentCreateDto dto, @Auth AuthUser authUser) {
		return paymentService.ready(dto, authUser);
	}

	@GetMapping
	public PageResponse<PaymentReadDto> readAll(
		@Auth AuthUser authUser,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size
	) {
		return paymentService.readAll(authUser, page, size);
	}

	@GetMapping("/kakao/success/{id}/{order}")
	public RedirectView approve(
		@RequestParam("pg_token") String token,
		@PathVariable("id") Long id,
		@PathVariable("order") String order
	) {
		final String galleryId = paymentService.approve(token, id, order);
		return new RedirectView(SUCCESS_REDIRECT_URL + galleryId + "/" + order);
	}

	@GetMapping("/kakao/cancel")
	public RedirectView cancel() {
		return new RedirectView(FAIL_REDIRECT_URL);
	}

	@GetMapping("/kakao/fail")
	public ResponseEntity<String> fail() {
		return ResponseEntity.internalServerError().body("결제 실패");
	}
}
