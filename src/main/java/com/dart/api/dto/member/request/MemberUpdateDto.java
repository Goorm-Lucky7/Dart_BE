package com.dart.api.dto.member.request;

import lombok.Builder;

@Builder
public record MemberUpdateDto(
	boolean isCheckedNickname,
	String nickname,
	String introduce
) {
}
