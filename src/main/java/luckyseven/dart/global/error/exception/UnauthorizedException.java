package luckyseven.dart.global.error.exception;

import luckyseven.dart.global.error.model.ErrorCode;

public class UnauthorizedException extends DartException {
	public UnauthorizedException(ErrorCode errorCode) { super(errorCode);}
}
