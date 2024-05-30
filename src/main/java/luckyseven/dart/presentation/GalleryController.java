package luckyseven.dart.presentation;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpStatus;
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
import luckyseven.dart.api.application.gallery.GalleryService;
import luckyseven.dart.api.dto.gallery.request.CreateGalleryDto;
import luckyseven.dart.api.dto.gallery.request.DeleteGalleryDto;

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
		try {
			if (createGalleryDto.images().size() > 20) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body("[❎ ERROR] 전시 작품은 최대 20개까지 생성 가능합니다.");
			}
			if (createGalleryDto.images().size() != imageFiles.size()) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body("[❎ ERROR] 전시 작품에 대한 정보를 잘못 입력하셨습니다.");
			}
			galleryService.createGallery(createGalleryDto, thumbnail, imageFiles);
			return ResponseEntity.ok("ok");
		} catch (IOException e) {
			return ResponseEntity.status(500).body("File upload error: " + e.getMessage());
		}
	}

	@DeleteMapping
	public ResponseEntity<String> deleteGalley(@RequestBody @Validated DeleteGalleryDto deleteGalleryDto) {
		galleryService.deleteGallery(deleteGalleryDto);
		return ResponseEntity.ok("ok");
	}

}
