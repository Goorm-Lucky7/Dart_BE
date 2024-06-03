package com.dart.global.error.exception;

import com.dart.global.error.model.ErrorCode;

public class DartException extends RuntimeException {
	private ErrorCode errorCode;

	public DartException(ErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}
}
