package com.dart.api.application.auth;

import static com.dart.global.common.util.AuthConstant.*;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dart.global.common.util.RedisUtil;
import com.dart.global.error.exception.InvalidVerificationCodeException;
import com.dart.global.error.exception.MailSendException;
import com.dart.global.error.model.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class EmailService {

	private final JavaMailSender emailSender;
	private final RedisUtil redisUtil;

	@Value("${spring.mail.username}")
	private String sender;

	@Value("${spring.mail.auth-code-expiration-millis}")
	private int authCodeExpirationMillis;

	public void sendEmail(String to) {
		String code = createCode();
		SimpleMailMessage emailForm = createEmailForm(to, EMAIL_TITLE, code);

		try {
			emailSender.send(emailForm);
			redisUtil.setDataExpire(to, code, authCodeExpirationMillis);
		} catch (RuntimeException e) {
			throw new MailSendException(ErrorCode.FAIL_EMAIL_SEND);
		}
	}

	private SimpleMailMessage createEmailForm(String to, String subject, String text) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(to);
		message.setSubject(subject);
		message.setText(text);
		message.setFrom(sender);

		return message;
	}

	public void verifyCode(String email, int code) {
		int storedCode = Integer.parseInt(redisUtil.getData(email));
		if (storedCode != code) {
			throw new InvalidVerificationCodeException(ErrorCode.FAIL_INCORRECT_EMAIL_CODE);
		}
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
}
