package luckyseven.dart.global.error.exception;

import luckyseven.dart.global.error.model.ErrorCode;

public class ServerErrorException extends DartException {
	public ServerErrorException(ErrorCode errorCode) { super(errorCode); }
}
