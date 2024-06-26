package com.dart.api.application.gallery;

import static com.dart.global.common.util.GlobalConstant.*;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.dart.api.domain.gallery.entity.Gallery;
import com.dart.api.domain.gallery.entity.Image;
import com.dart.api.domain.gallery.repository.ImageRepository;
import com.dart.api.dto.gallery.request.ImageInfoDto;
import com.dart.api.dto.gallery.response.ImageResDto;
import com.dart.api.infrastructure.s3.S3Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ImageService {

	private final ImageRepository imageRepository;
	private final S3Service s3Service;
	private final GalleryProgressService galleryProgressService;

	public void saveImages(List<ImageInfoDto> imageInfoDtos, List<MultipartFile> imageFiles, Gallery gallery,
		Long memberId) {
		int totalFiles = imageInfoDtos.size();
		for (int i = 0; i < totalFiles; i++) {
			ImageInfoDto imageInfoDto = imageInfoDtos.get(i);
			MultipartFile imageFile = imageFiles.get(i);
			processImage(imageInfoDto, imageFile, gallery);
			int progress = (int)((i + INCREMENT_BY_ONE) / (double)totalFiles * ONE_HUNDRED_PERCENT);
			galleryProgressService.sendProgress(memberId, progress);
		}
	}

	private void processImage(ImageInfoDto imageInfoDto, MultipartFile imageFile, Gallery gallery) {
		String imageUrl = s3Service.uploadFile(imageFile);
		final Image image = createImageEntity(imageInfoDto, imageUrl, gallery);
		imageRepository.save(image);
	}

	private Image createImageEntity(ImageInfoDto imageInfoDto, String imageUrl, Gallery gallery) {
		return Image.builder()
			.imageUri(imageUrl)
			.imageTitle(imageInfoDto.imageTitle())
			.description(imageInfoDto.description())
			.gallery(gallery)
			.build();
	}

	public void deleteImagesByGallery(Gallery gallery) {
		List<Image> images = imageRepository.findByGallery(gallery);
		for (Image image : images) {
			s3Service.deleteFile(image.getImageUri());
			imageRepository.delete(image);
		}
	}

	public void deleteThumbnail(Gallery gallery) {
		s3Service.deleteFile(gallery.getThumbnail());
	}

	@Transactional(readOnly = true)
	public List<ImageResDto> getImagesByGalleryId(Long galleryId) {
		return imageRepository.findByGalleryId(galleryId).stream()
			.map(image -> new ImageResDto(image.getImageUri(), image.getDescription(), image.getImageTitle()))
			.collect(Collectors.toList());
	}
}
