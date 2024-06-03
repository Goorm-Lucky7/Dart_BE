package com.dart.global.error.exception;

import com.dart.global.error.model.ErrorCode;

public class ConflictException extends DartException {
	public ConflictException(ErrorCode errorCode) { super(errorCode); }
}
