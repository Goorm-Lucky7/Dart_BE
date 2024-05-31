package luckyseven.dart.api.application.member;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import luckyseven.dart.api.domain.member.repo.MemberRepository;
import luckyseven.dart.global.error.exception.ConflictException;
import luckyseven.dart.global.error.model.ErrorCode;

@Component
@RequiredArgsConstructor
public class NicknameValidator {

	private final MemberRepository memberRepository;

	public void validate(String nickname) {
		if (nickname == null || nickname.trim().isEmpty()) {
			throw new IllegalArgumentException(String.valueOf(ErrorCode.FAIL_INVALID_NICKNAME_FORMAT));
		}
		if (memberRepository.existsByNickname(nickname)) {
			throw new ConflictException(ErrorCode.FAIL_NICKNAME_CONFLICT);
		}
	}
}
