package com.dart.api.application.auth;

import static com.dart.global.common.util.AuthConstant.*;
import static java.lang.Boolean.*;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dart.api.domain.auth.repository.EmailRedisRepository;
import com.dart.api.domain.auth.repository.SessionRedisRepository;
import com.dart.api.domain.member.repository.MemberRepository;
import com.dart.global.common.util.CookieUtil;
import com.dart.global.error.exception.BadRequestException;
import com.dart.global.error.exception.ConflictException;
import com.dart.global.error.exception.MailSendException;
import com.dart.global.error.exception.NotFoundException;
import com.dart.global.error.model.ErrorCode;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class EmailService {

	private final JavaMailSender emailSender;
	private final EmailRedisRepository emailRedisRepository;
	private final MemberRepository memberRepository;
	private final SessionRedisRepository sessionRedisRepository;

	private final CookieUtil cookieUtil;

	@Value("${spring.mail.username}")
	private String sender;

	public void sendVerificationEmail(String newReceiver, String sessionId, HttpServletResponse response) {
		validateEmail(newReceiver);
		updateReceiverEmail(sessionId, newReceiver);
		sessionId = checkAndSetSessionId(sessionId, response);

		final String code = createCode();
		sendEmail(newReceiver, code);

		emailRedisRepository.setEmail(newReceiver, code);
		sessionRedisRepository.saveSessionEmailMapping(sessionId, newReceiver);
	}

	public void verifyEmail(String to, String code) {
		validateExpired(to);
		validateCode(to, code);
		emailRedisRepository.setVerified(to);
	}

	private String checkAndSetSessionId(String sessionId, HttpServletResponse response) {
		if (sessionId == null || sessionId.isEmpty()) {
			sessionId = cookieUtil.setSessionCookie(response);
		}

		return sessionId;
	}

	private void updateReceiverEmail(String sessionId, String newReceiver) {
		String oldReceiver = sessionRedisRepository.findEmailBySessionId(sessionId);
		if (oldReceiver != null && !oldReceiver.equals(newReceiver)) {
			emailRedisRepository.deleteEmail(oldReceiver);
		}
	}

	private void sendEmail(String to, String code) {
		SimpleMailMessage emailForm = createEmailForm(to, EMAIL_TITLE, code);
		emailSender.send(emailForm);
	}

	private SimpleMailMessage createEmailForm(String to, String subject, String text) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(to);
		message.setSubject(subject);
		message.setText(text);
		message.setFrom(sender);

		return message;
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
		if(!emailRedisRepository.checkExistsEmail(to)) {
			throw new NotFoundException(ErrorCode.FAIL_INVALID_EMAIL_CODE);
		}
	}

	private void validateCode(String to, String code) {
		if(TRUE.equals(emailRedisRepository.isVerified(to))){
			throw new BadRequestException(ErrorCode.FAIL_ALREADY_VERIFIED_EMAIL);
		}
		if(!code.equals(emailRedisRepository.findVerificationCodeByEmail(to))) {
			throw new BadRequestException(ErrorCode.FAIL_INCORRECT_EMAIL_CODE);
		}
	}

	private void validateEmail(String receiver){
		if(memberRepository.existsByEmail(receiver)){
			throw new ConflictException(ErrorCode.FAIL_EMAIL_CONFLICT);
		}
	}
}
