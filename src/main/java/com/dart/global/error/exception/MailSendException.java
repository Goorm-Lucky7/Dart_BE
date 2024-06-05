package com.dart.global.error.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.dart.global.error.model.ErrorCode;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class MailSendException extends DartException {
	public MailSendException(ErrorCode errorCode) { super(errorCode); }
}
