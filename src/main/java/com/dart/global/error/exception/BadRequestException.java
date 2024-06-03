package com.dart.global.error.exception;

import com.dart.global.error.model.ErrorCode;

public class BadRequestException extends DartException {
	public BadRequestException(ErrorCode errorCode) { super(errorCode); }
}
