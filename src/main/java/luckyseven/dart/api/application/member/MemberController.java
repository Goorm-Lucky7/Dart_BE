package luckyseven.dart.api.application.member;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import luckyseven.dart.api.domain.auth.AuthUser;
import luckyseven.dart.api.dto.member.request.LoginReqDto;
import luckyseven.dart.api.dto.member.request.MemberUpdateDto;
import luckyseven.dart.api.dto.member.request.SignUpDto;
import luckyseven.dart.api.dto.member.response.LoginResDto;
import luckyseven.dart.api.application.auth.AuthenticationService;
import luckyseven.dart.api.dto.member.response.MemberProfileResDto;
import luckyseven.dart.global.auth.annotation.Auth;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class MemberController {

	private final MemberService memberService;
	private final AuthenticationService authenticationService;

	@PostMapping("/signup")
	@ResponseStatus(HttpStatus.CREATED)
	public ResponseEntity<String> signUp(@RequestBody @Validated SignUpDto signUpDto) {
		memberService.signUp(signUpDto);
		return ResponseEntity.ok("OK");
	}

	@PostMapping("/login")
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<LoginResDto> login(
		@RequestBody @Validated LoginReqDto loginReqDto, HttpServletResponse response
	) {
		return ResponseEntity.ok(authenticationService.login(loginReqDto, response));
	}

	@GetMapping("/members")
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<MemberProfileResDto> getMemberProfile(@Auth AuthUser authUser) {
		return ResponseEntity.ok( memberService.getMemberProfile(authUser));
	}

	@PutMapping("/members")
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<String> updateMemberProfile(@Auth AuthUser authUser,
		@RequestBody @Valid MemberUpdateDto memberUpdateDto) {
		memberService.updateMemberProfile(authUser, memberUpdateDto);
		return ResponseEntity.ok("OK");
	}

}
