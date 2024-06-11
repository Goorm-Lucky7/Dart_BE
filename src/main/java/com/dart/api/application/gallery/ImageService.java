package com.dart.api.application.gallery;

import java.io.IOException;
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
import com.dart.global.error.exception.BadRequestException;
import com.dart.global.error.model.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ImageService {

	private final ImageRepository imageRepository;
	private final S3Service s3Service;

	public void saveImages(List<ImageInfoDto> imageInfoDtos, List<MultipartFile> imageFiles, Gallery gallery) {
		if (imageInfoDtos != null && !imageInfoDtos.isEmpty()) {
			for (int i = 0; i < imageInfoDtos.size(); i++) {
				ImageInfoDto imageInfoDto = imageInfoDtos.get(i);
				MultipartFile imageFile = imageFiles.get(i);
				processImage(imageInfoDto, imageFile, gallery);
			}
		}
	}

	private void processImage(ImageInfoDto imageInfoDto, MultipartFile imageFile, Gallery gallery) {
		try {
			String imageUrl = s3Service.uploadFile(imageFile);
			final Image image = createImageEntity(imageInfoDto, imageUrl, gallery);
			imageRepository.save(image);
		} catch (IOException e) {
			throw new BadRequestException(ErrorCode.FAIL_INVALID_REQUEST);
		}
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
