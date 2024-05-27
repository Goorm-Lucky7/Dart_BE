package luckyseven.dart.global.error.exception;

import luckyseven.dart.global.error.model.ErrorCode;

public class ForbiddenException extends DartException {
	public ForbiddenException(ErrorCode errorCode) { super(errorCode); }
}
