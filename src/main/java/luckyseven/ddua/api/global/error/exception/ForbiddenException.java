package luckyseven.ddua.api.global.error.exception;

import luckyseven.ddua.api.global.error.model.ErrorCode;

public class ForbiddenException extends DduaException {
	public ForbiddenException(ErrorCode errorCode) { super(errorCode); }
}
