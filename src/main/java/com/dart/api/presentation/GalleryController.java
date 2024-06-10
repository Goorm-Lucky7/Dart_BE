package com.dart.api.presentation;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.dart.api.application.gallery.GalleryService;
import com.dart.api.domain.auth.entity.AuthUser;
import com.dart.api.dto.gallery.request.CreateGalleryDto;
import com.dart.api.dto.gallery.request.DeleteGalleryDto;
import com.dart.api.dto.gallery.response.GalleryAllResDto;
import com.dart.api.dto.gallery.response.GalleryInfoDto;
import com.dart.api.dto.page.PageResponse;
import com.dart.global.auth.annotation.Auth;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/galleries")
@RequiredArgsConstructor
public class GalleryController {

	private final GalleryService galleryService;

	@PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
	public ResponseEntity<String> createGallery(
		@RequestPart("gallery") @Validated CreateGalleryDto createGalleryDto,
		@RequestPart("thumbnail") MultipartFile thumbnail,
		@RequestPart("images") List<MultipartFile> imageFiles,
		@Auth AuthUser authUser) {
		galleryService.createGallery(createGalleryDto, thumbnail, imageFiles, authUser);
		return ResponseEntity.ok("OK");
	}

	@DeleteMapping
	public ResponseEntity<String> deleteGalley(@RequestBody @Validated DeleteGalleryDto deleteGalleryDto,
		@Auth AuthUser authUser) {
		galleryService.deleteGallery(deleteGalleryDto, authUser);
		return ResponseEntity.ok("OK");
	}

	@GetMapping
	public PageResponse<GalleryAllResDto> getAllGalleries(
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size,
		@RequestParam(required = false) String category,
		@RequestParam(required = false) String keyword,
		@RequestParam(required = false) String sort,
		@RequestParam(required = false) String cost,
		@RequestParam(required = false) String display,
		@Auth(required = false) AuthUser authUser
	) {
		return galleryService.getAllGalleries(page, size, category, keyword, sort, cost, display, authUser);
	}

	@GetMapping("/info")
	public ResponseEntity<GalleryInfoDto> getGalleryInfo(@RequestParam("gallery-id") Long galleryId,
		@Auth(required = false) AuthUser authUser) {
		return ResponseEntity.ok(galleryService.getGalleryInfo(galleryId, authUser));
	}
}
