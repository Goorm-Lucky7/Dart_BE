package luckyseven.dart.api.dto.member.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record MemberUpdateDto(
	@Size(min = 2, max = 10, message = "[❎ ERROR] 닉네임은 2글자에서 10글자 사이여야 합니다.")
	@Pattern(regexp = "^[A-Za-z\\d가-힣]+$", message = "[❎ ERROR] 닉네임은 한글과 영어만 사용가능합니다.")
	String nickname,

	@NotBlank(message = "[❎ ERROR] 비밀번호를 입력해주세요.")
	@Size(min = 8, max = 20, message = "[❎ ERROR] 비밀번호는 8글자에서 20글자 사이여야 합니다.")
	@Pattern(
		regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[~!@#$%^&*\\-_+=`|\\\\(){}\\[\\]:;\"'<>,.?/]).+$",
		message = "[❎ ERROR] 비밀번호는 영문, 숫자, 특정 특수문자(~!@#$%^&*\\-_+=`|\\(){}[]:;\"'<>,.?/)를 각각 최소 하나 이상 포함해야 합니다."
	)
	String password,

	String bank,
	String account,
	String introduce,
	String profileImage
) {
}
