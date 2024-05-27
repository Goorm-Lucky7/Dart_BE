package luckyseven.dart.global.error.exception;

import luckyseven.dart.global.error.model.ErrorCode;

public class NotFoundException extends DartException {
	public NotFoundException(ErrorCode errorCode) { super(errorCode);}
}
