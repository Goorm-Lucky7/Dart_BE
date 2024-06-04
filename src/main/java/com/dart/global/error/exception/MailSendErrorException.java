package com.dart.global.error.exception;

import com.dart.global.error.model.ErrorCode;

public class MailSendErrorException extends DartException {
	public MailSendErrorException(ErrorCode errorCode) { super(errorCode); }
}
