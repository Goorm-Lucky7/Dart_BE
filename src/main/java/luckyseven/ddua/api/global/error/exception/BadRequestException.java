package luckyseven.ddua.api.global.error.exception;

import luckyseven.ddua.api.global.error.model.ErrorCode;

public class BadRequestException extends DduaException {
	public BadRequestException(ErrorCode errorCode) { super(errorCode); }
}
