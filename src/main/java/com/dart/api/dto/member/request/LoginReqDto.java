package com.dart.api.dto.member.request;

import jakarta.validation.constraints.NotBlank;

public record LoginReqDto(
	@NotBlank(message = "[❎ ERROR] 이메일을 입력해주세요.")
	String email,

	@NotBlank(message = "[❎ ERROR] 비밀번호를 입력해주세요.")
	String password
) {
}
