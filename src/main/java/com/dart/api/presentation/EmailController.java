package com.dart.api.presentation;

import static com.dart.global.common.util.AuthConstant.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.dart.api.application.auth.EmailService;
import com.dart.api.dto.auth.request.EmailSendReqDto;
import com.dart.api.dto.auth.request.EmailVerificationReqDto;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/email")
public class EmailController {

	private final EmailService emailService;

	@PostMapping("/send")
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<String> sendVerificationEmail(@RequestBody @Validated EmailSendReqDto emailSendReqDto,
		@CookieValue(value = SESSION_ID, required = false) String sessionId, HttpServletResponse response) {
		emailService.sendVerificationEmail(emailSendReqDto.email(), sessionId, response);
		return ResponseEntity.ok("OK");
	}

	@PostMapping("/verify")
	public ResponseEntity<String> verifyEmail(@RequestBody @Validated EmailVerificationReqDto emailVerificationReqDto) {
		emailService.verifyEmail(emailVerificationReqDto.email(), emailVerificationReqDto.code());
		return ResponseEntity.ok("OK");
	}
}
