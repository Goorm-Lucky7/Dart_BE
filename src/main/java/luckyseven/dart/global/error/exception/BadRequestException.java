package luckyseven.dart.global.error.exception;

import luckyseven.dart.global.error.model.ErrorCode;

public class BadRequestException extends DartException {
	public BadRequestException(ErrorCode errorCode) { super(errorCode); }
}
