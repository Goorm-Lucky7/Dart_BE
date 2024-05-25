package luckyseven.ddua.api.global.error.exception;

import luckyseven.ddua.api.global.error.model.ErrorCode;

public class NotFoundException extends DduaException {
	public NotFoundException(ErrorCode errorCode) { super(errorCode);}
}
