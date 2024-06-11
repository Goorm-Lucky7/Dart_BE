package com.dart.api.presentation;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.dart.api.application.auth.AuthenticationService;
import com.dart.api.application.member.MemberService;
import com.dart.api.domain.auth.entity.AuthUser;
import com.dart.api.dto.auth.response.TokenResDto;
import com.dart.api.dto.member.request.LoginReqDto;
import com.dart.api.dto.member.request.MemberUpdateDto;
import com.dart.api.dto.member.request.NicknameDuplicationCheckDto;
import com.dart.api.dto.member.request.SignUpDto;
import com.dart.api.dto.member.response.LoginResDto;
import com.dart.api.dto.member.response.MemberProfileResDto;
import com.dart.global.auth.annotation.Auth;

import jakarta.servlet.http.HttpServletRequest;
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
		return ResponseEntity.ok("Signup successfully");
	}

	@PostMapping("/login")
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<LoginResDto> login(
		@RequestBody @Validated LoginReqDto loginReqDto, HttpServletResponse response) {
		return ResponseEntity.ok(authenticationService.login(loginReqDto, response));
	}

	@PostMapping("/reissue")
	@ResponseStatus(HttpStatus.CREATED)
	public ResponseEntity<TokenResDto> reissue(HttpServletRequest request, HttpServletResponse response) {
		return ResponseEntity.ok(authenticationService.reissue(request, response));
	}

	@GetMapping("/members/{nickname}")
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<MemberProfileResDto> getMemberProfile(
		@PathVariable(name = "nickname") String nickname, @Auth AuthUser authUser) {
		return ResponseEntity.ok(memberService.getMemberProfile(nickname, authUser));
	}

	@PutMapping("/members")
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<String> updateMemberProfile(@Auth AuthUser authUser,
		@RequestPart @Valid MemberUpdateDto memberUpdateDto,
		@RequestPart(name = "profileImage", required = false) MultipartFile profileImage) {
		memberService.updateMemberProfile(authUser, memberUpdateDto, profileImage);
		return ResponseEntity.ok("Updated member profile successfully");
	}

	@PostMapping("/nickname/check")
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<String> checkNicknameDuplication(
		@RequestBody NicknameDuplicationCheckDto nicknameDuplicationCheckDto) {
		memberService.checkNicknameDuplication(nicknameDuplicationCheckDto);

		return ResponseEntity.ok("Checked nickname duplication successfully");
	}
}
