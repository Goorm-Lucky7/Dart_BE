package com.dart.presentation;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import com.dart.api.application.gallery.GalleryService;
import com.dart.dto.gallery.request.CreateGalleryDto;
import com.dart.dto.gallery.request.DeleteGalleryDto;

@RestController
@RequestMapping("/api/galleries")
@RequiredArgsConstructor
public class GalleryController {

	private final GalleryService galleryService;

	@PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
	public ResponseEntity<String> createGallery(
		@RequestPart("gallery") @Validated CreateGalleryDto createGalleryDto,
		@RequestPart("thumbnail") MultipartFile thumbnail,
		@RequestPart("images") List<MultipartFile> imageFiles) {
		galleryService.createGallery(createGalleryDto, thumbnail, imageFiles);
		return ResponseEntity.ok("ok");
	}

	@DeleteMapping
	public ResponseEntity<String> deleteGalley(@RequestBody @Validated DeleteGalleryDto deleteGalleryDto) {
		galleryService.deleteGallery(deleteGalleryDto);
		return ResponseEntity.ok("ok");
	}

}
