package luckyseven.dart.api.dto.member.request;

import jakarta.validation.constraints.NotBlank;

public record NicknameDuplicationCheckDto(
	@NotBlank(message = "[❎ ERROR] 닉네임을 입력해주세요.")
	String nickname
) {
}
