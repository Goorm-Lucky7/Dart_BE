package luckyseven.dart.api.application.member;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import luckyseven.dart.api.dto.member.request.LoginReqDto;
import luckyseven.dart.api.dto.member.request.SignUpDto;
import luckyseven.dart.api.dto.member.response.LoginResDto;
import luckyseven.dart.api.application.auth.AuthenticationService;

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

}
