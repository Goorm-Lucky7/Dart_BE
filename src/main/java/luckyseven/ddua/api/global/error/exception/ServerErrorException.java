package luckyseven.ddua.api.global.error.exception;

import luckyseven.ddua.api.global.error.model.ErrorCode;

public class ServerErrorException extends DduaException {
	public ServerErrorException(ErrorCode errorCode) { super(errorCode); }
}
