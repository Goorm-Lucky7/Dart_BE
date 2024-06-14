package com.dart.api.presentation;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dart.api.application.gallery.GalleryService;
import com.dart.api.domain.auth.entity.AuthUser;
import com.dart.api.dto.gallery.response.GalleryMypageResDto;
import com.dart.api.dto.page.PageResponse;
import com.dart.global.auth.annotation.Auth;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
public class MypageController {
	private final GalleryService galleryService;

	@GetMapping
	public PageResponse<GalleryMypageResDto> getMypageGalleries(
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size,
		@RequestParam(required = false) String nickname,
		@Auth(required = false) AuthUser authUser) {
		return galleryService.getMypageGalleries(page, size, nickname, authUser);
	}
}
