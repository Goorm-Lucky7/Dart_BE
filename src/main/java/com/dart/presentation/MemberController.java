package com.dart.presentation;

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

import com.dart.api.application.auth.AuthenticationService;
import com.dart.api.application.member.MemberService;
import com.dart.api.domain.auth.AuthUser;
import com.dart.dto.member.request.LoginReqDto;
import com.dart.dto.member.request.MemberUpdateDto;
import com.dart.dto.member.request.NicknameDuplicationCheckDto;
import com.dart.dto.member.request.SignUpDto;
import com.dart.dto.member.response.LoginResDto;
import com.dart.dto.member.response.MemberProfileResDto;
import com.dart.global.auth.annotation.Auth;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


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

	@PostMapping("/members/nickname/check")
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<String> checkNicknameDuplication(
		@RequestBody NicknameDuplicationCheckDto nicknameDuplicationCheckDto) {
		memberService.checkNicknameDuplication(nicknameDuplicationCheckDto);

		return ResponseEntity.ok("OK");
	}
}
