package luckyseven.ddua.api.global.error.exception;

import luckyseven.ddua.api.global.error.model.ErrorCode;

public class UnauthorizedException extends DduaException {
	public UnauthorizedException(ErrorCode errorCode) { super(errorCode);}
}
