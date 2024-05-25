package luckyseven.ddua.api.global.error.exception;

import luckyseven.ddua.api.global.error.model.ErrorCode;

public class ConflictException extends DduaException {
	public ConflictException(ErrorCode errorCode) { super(errorCode); }
}
