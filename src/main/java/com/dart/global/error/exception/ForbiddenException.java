package com.dart.global.error.exception;

import com.dart.global.error.model.ErrorCode;

public class ForbiddenException extends DartException {
	public ForbiddenException(ErrorCode errorCode) { super(errorCode); }
}
