package com.dart.global.error.exception;

import com.dart.global.error.model.ErrorCode;

public class NotFoundException extends DartException {
	public NotFoundException(ErrorCode errorCode) { super(errorCode);}
}
