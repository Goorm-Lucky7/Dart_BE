package luckyseven.dart.global.error.exception;

import luckyseven.dart.global.error.model.ErrorCode;

public class ConflictException extends DartException {
	public ConflictException(ErrorCode errorCode) { super(errorCode); }
}
