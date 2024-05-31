package com.dart.global.error.exception;

import com.dart.global.error.model.ErrorCode;

public class UnauthorizedException extends DartException {
	public UnauthorizedException(ErrorCode errorCode) { super(errorCode);}
}
