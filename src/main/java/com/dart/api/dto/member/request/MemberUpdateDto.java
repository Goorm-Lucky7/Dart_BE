package com.dart.api.dto.member.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record MemberUpdateDto(
	@Size(min = 2, max = 10, message = "[❎ ERROR] 닉네임은 2글자에서 10글자 사이여야 합니다.")
	@Pattern(regexp = "^[A-Za-z\\d가-힣]+$", message = "[❎ ERROR] 닉네임은 한글과 영어만 사용가능합니다.")
	String nickname,

	String profileImage,
	String bank,
	String account,
	String introduce
) {
}
