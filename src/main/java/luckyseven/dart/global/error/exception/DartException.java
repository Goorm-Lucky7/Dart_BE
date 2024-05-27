package luckyseven.dart.global.error.exception;

import luckyseven.dart.global.error.model.ErrorCode;

public class DartException extends RuntimeException {
	private ErrorCode errorCode;

	public DartException(ErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}
}
