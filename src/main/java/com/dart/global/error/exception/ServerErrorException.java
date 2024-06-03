package com.dart.global.error.exception;

import com.dart.global.error.model.ErrorCode;

public class ServerErrorException extends DartException {
	public ServerErrorException(ErrorCode errorCode) { super(errorCode); }
}
