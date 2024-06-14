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

import com.dart.api.infrastructure.redis.EmailRedisRepository;
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
	private final EmailRedisRepository emailRedisRepository;

	@Value("${spring.mail.username}")
	private String sender;

	public void sendEmail(String to) {
		String code = createCode();
		SimpleMailMessage emailForm = createEmailForm(to, EMAIL_TITLE, code);

		try {
			if(emailRedisRepository.checkExistsEmail(to)) emailRedisRepository.deleteEmail(to);
			emailSender.send(emailForm);
			emailRedisRepository.setEmail(to, code);
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

	public void verifyCode(String to, int code) {
		int storedCode = Integer.parseInt(emailRedisRepository.getEmail(to));
		if (storedCode != code) {
			throw new InvalidVerificationCodeException(ErrorCode.FAIL_INCORRECT_EMAIL_CODE);
		}
		emailRedisRepository.deleteEmail(to);
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
