package com.dart.global.error.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.dart.global.error.model.ErrorCode;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidVerificationCodeException extends DartException{
	public InvalidVerificationCodeException(ErrorCode errorCode) { super(errorCode); }
}
