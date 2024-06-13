package com.dart.api.application.auth;

import static com.dart.api.infrastructure.redis.RedisConstant.*;
import static com.dart.global.common.util.AuthConstant.*;
import static com.dart.global.common.util.GlobalConstant.*;
import static java.lang.Boolean.*;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dart.api.infrastructure.redis.RedisEmailRepository;
import com.dart.api.infrastructure.redis.RedisSessionRepository;
import com.dart.global.error.exception.BadRequestException;
import com.dart.global.error.exception.MailSendException;
import com.dart.global.error.exception.NotFoundException;
import com.dart.global.error.model.ErrorCode;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class EmailService {

	private final JavaMailSender emailSender;
	private final RedisEmailRepository redisEmailRepository;
	private final RedisSessionRepository redisSessionRepository;

	@Value("${spring.mail.username}")
	private String sender;

	public void sendVerificationEmail(String newReceiver, String sessionId, HttpServletResponse response) {
		if (sessionId == null || sessionId.isEmpty()) {
			setCookie(response);
		}

		String oldReceiver = redisSessionRepository.findEmailBySessionId(sessionId);
		if (oldReceiver != null && !oldReceiver.equals(newReceiver)) {
			redisEmailRepository.deleteEmail(oldReceiver);
		}

		String code = createCode();
		SimpleMailMessage emailForm = createEmailForm(newReceiver, EMAIL_TITLE, code);

		emailSender.send(emailForm);
		redisEmailRepository.setEmail(newReceiver, code);
		redisSessionRepository.saveSessionEmailMapping(sessionId, newReceiver);
	}

	private SimpleMailMessage createEmailForm(String to, String subject, String text) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(to);
		message.setSubject(subject);
		message.setText(text);
		message.setFrom(sender);

		return message;
	}

	private void setCookie(HttpServletResponse response){
		String sessionId = UUID.randomUUID().toString();
		ResponseCookie cookie = ResponseCookie.from(SESSION_ID, sessionId)
			.httpOnly(true)
			.secure(true)
			.path("/")
			.maxAge(THIRTY_MINUTES)
			.sameSite("None")
			.build();

		response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
	}

	public void verifyEmail(String to, int code) {
		validateExpired(to);
		validateCode(to, code);

		redisEmailRepository.setVerified(to);
	}

	private String createCode() {
		try {
			Random random = SecureRandom.getInstanceStrong();
			StringBuilder sb = new StringBuilder();

			for (int i = 0; i < EMAIL_CODE_LENGTH; i++) {
				sb.append(random.nextInt(10));
			}

			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			throw new MailSendException(ErrorCode.FAIL_EMAIL_SEND);
		}
	}

	private void validateExpired(String to) {
		if(!redisEmailRepository.existsEmail(to)) {
			throw new NotFoundException(ErrorCode.FAIL_INVALID_EMAIL_CODE);
		}
	}

	private void validateCode(String to, int code) {
		if(TRUE.equals(redisEmailRepository.isVerified(to))){
			throw new BadRequestException(ErrorCode.FAIL_ALREADY_VERIFIED_EMAIL);
		}
		if(!String.valueOf(code).equals(redisEmailRepository.findVerificationCodeByEmail(to))) {
			throw new BadRequestException(ErrorCode.FAIL_INCORRECT_EMAIL_CODE);
		}
	}
}
