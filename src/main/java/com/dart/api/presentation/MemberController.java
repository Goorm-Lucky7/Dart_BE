package com.dart.api.presentation;

import static com.dart.global.common.util.AuthConstant.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class MemberController {

	private final MemberService memberService;
	private final AuthenticationService authenticationService;

	@PostMapping("/signup")
	@ResponseStatus(HttpStatus.CREATED)
	public ResponseEntity<String> signUp(@RequestBody @Validated SignUpDto signUpDto,
		@CookieValue(value = SESSION_ID, required = false) String sessionId) {
		memberService.signUp(signUpDto, sessionId);
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

	@GetMapping("/members")
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<MemberProfileResDto> getMemberProfile(
		@RequestParam String nickname, @Auth(required = false) AuthUser authUser) {
		return ResponseEntity.ok(memberService.getMemberProfile(nickname, authUser));
	}

	@PutMapping(path = "/members", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<String> updateMemberProfile(@Auth AuthUser authUser,
		@RequestPart @Validated MemberUpdateDto memberUpdateDto,
		@RequestPart(name = "profileImage", required = false) MultipartFile profileImage,
		@CookieValue(value = SESSION_ID, required = false) String sessionId) {
		memberService.updateMemberProfile(authUser, memberUpdateDto, profileImage, sessionId);
		return ResponseEntity.ok("Updated member profile successfully");
	}

	@PostMapping("/nickname/check")
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<String> checkNicknameDuplication(
		@RequestBody @Validated NicknameDuplicationCheckDto nicknameDuplicationCheckDto,
		@CookieValue(value = SESSION_ID, required = false) String sessionId, HttpServletResponse response) {
		memberService.checkNicknameDuplication(nicknameDuplicationCheckDto, sessionId, response);
		return ResponseEntity.ok("Checked nickname duplication successfully");
	}
}
