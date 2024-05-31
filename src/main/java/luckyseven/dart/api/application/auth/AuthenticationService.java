package luckyseven.dart.api.application.auth;


import static luckyseven.dart.global.common.util.AuthConstant.*;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import luckyseven.dart.api.domain.member.entity.Member;
import luckyseven.dart.api.domain.member.repo.MemberRepository;
import luckyseven.dart.dto.auth.TokenReqDto;
import luckyseven.dart.dto.auth.TokenResDto;
import luckyseven.dart.dto.member.request.LoginReqDto;
import luckyseven.dart.dto.member.response.LoginResDto;
import luckyseven.dart.global.error.exception.BadRequestException;
import luckyseven.dart.global.error.exception.NotFoundException;
import luckyseven.dart.global.error.model.ErrorCode;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtProviderService jwtProviderService;

	@Transactional
	public LoginResDto login(LoginReqDto loginReqDto, HttpServletResponse response) {
		final Member member = findByMemberEmail(loginReqDto.email());
		validatePasswordMatch(loginReqDto.password(), member.getPassword());

		final String accessToken = jwtProviderService.generateToken(member.getEmail(), member.getNickname());

		response.setHeader(ACCESS_TOKEN_HEADER, accessToken);

		return new LoginResDto(accessToken);
	}

	public TokenResDto reissue(TokenReqDto tokenReqDto, HttpServletResponse response) {
		final String newAccessToken = jwtProviderService.reGenerateToken(tokenReqDto.expiredToken());

		response.setHeader(ACCESS_TOKEN_HEADER, newAccessToken);
		response.setStatus(HttpServletResponse.SC_CREATED);

		return new TokenResDto(newAccessToken);
	}

	private Member findByMemberEmail(String email) {
		return memberRepository.findByEmail(email)
			.orElseThrow(() -> new NotFoundException(ErrorCode.FAIL_MEMBER_NOT_FOUND));
	}

	private void validatePasswordMatch(String password, String encodedPassword) {
		if (!passwordEncoder.matches(password, encodedPassword)) {
			throw new BadRequestException(ErrorCode.FAIL_INCORRECT_PASSWORD);
		}
	}
}
