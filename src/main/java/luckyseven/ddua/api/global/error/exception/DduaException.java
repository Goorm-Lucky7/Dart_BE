package luckyseven.ddua.api.global.error.exception;

import luckyseven.ddua.api.global.error.model.ErrorCode;

public class DduaException extends RuntimeException {
	private ErrorCode errorCode;

	public DduaException(ErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}
}
