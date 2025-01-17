package com.dart.api.presentation;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.dart.api.application.gallery.GalleryProgressService;
import com.dart.api.application.gallery.GalleryService;
import com.dart.api.domain.auth.entity.AuthUser;
import com.dart.api.dto.gallery.request.CreateGalleryDto;
import com.dart.api.dto.gallery.request.DeleteGalleryDto;
import com.dart.api.dto.gallery.response.GalleryAllResDto;
import com.dart.api.dto.gallery.response.GalleryInfoDto;
import com.dart.api.dto.gallery.response.GalleryReadIdDto;
import com.dart.api.dto.gallery.response.GalleryResDto;
import com.dart.api.dto.page.PageResponse;
import com.dart.global.auth.annotation.Auth;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/galleries")
@RequiredArgsConstructor
public class GalleryController {

	private final GalleryService galleryService;
	private final GalleryProgressService galleryProgressService;

	@PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
	public GalleryReadIdDto createGallery(
		@RequestPart("gallery") @Validated CreateGalleryDto createGalleryDto,
		@RequestPart("thumbnail") MultipartFile thumbnail,
		@RequestPart("images") List<MultipartFile> imageFiles,
		@Auth AuthUser authUser) {
		return galleryService.createGallery(createGalleryDto, thumbnail, imageFiles, authUser);
	}

	@GetMapping(value = "/progress", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public ResponseEntity<SseEmitter> getProgress(@Auth AuthUser authUser) {
		return ResponseEntity.ok(galleryProgressService.createEmitter(authUser));
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
		@RequestParam(required = false) String display
	) {
		return galleryService.getAllGalleries(page, size, category, keyword, sort, cost, display);
	}

	@GetMapping("/{gallery-id}")
	public ResponseEntity<GalleryResDto> getGallery(
		@PathVariable("gallery-id") Long galleryId, @Auth(required = false) AuthUser authUser) {
		return ResponseEntity.ok(galleryService.getGallery(galleryId, authUser));
	}

	@GetMapping("/info")
	public ResponseEntity<GalleryInfoDto> getGalleryInfo(@RequestParam("gallery-id") Long galleryId,
		@Auth(required = false) AuthUser authUser) {
		return ResponseEntity.ok(galleryService.getGalleryInfo(galleryId, authUser));
	}

	@PutMapping("/reexhibition")
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<String> updateReExhibitionRequestCount(@RequestParam("gallery-id") Long galleryId) {
		galleryService.updateReExhibitionRequestCount(galleryId);
		return ResponseEntity.ok("OK");
	}
}
