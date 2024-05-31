package luckyseven.dart.api.application.gallery;

import static luckyseven.dart.global.common.util.GlobalConstant.*;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import luckyseven.dart.api.domain.gallery.entity.Cost;
import luckyseven.dart.api.domain.gallery.entity.Gallery;
import luckyseven.dart.api.domain.gallery.entity.Hashtag;
import luckyseven.dart.api.domain.gallery.entity.Image;
import luckyseven.dart.api.domain.gallery.repo.GalleryRepository;
import luckyseven.dart.api.domain.gallery.repo.HashtagRepository;
import luckyseven.dart.api.domain.gallery.repo.ImageRepository;
import luckyseven.dart.api.dto.gallery.request.CreateGalleryDto;
import luckyseven.dart.api.dto.gallery.request.DeleteGalleryDto;
import luckyseven.dart.api.dto.gallery.request.ImageInfoDto;
import luckyseven.dart.global.common.util.S3Service;
import luckyseven.dart.global.error.exception.BadRequestException;
import luckyseven.dart.global.error.exception.NotFoundException;
import luckyseven.dart.global.error.model.ErrorCode;

@Service
@RequiredArgsConstructor
@Transactional
public class GalleryService {

	private final GalleryRepository galleryRepository;
	private final HashtagRepository hashtagRepository;
	private final ImageRepository imageRepository;
	private final S3Service s3Service;

	public void createGallery(CreateGalleryDto createGalleryDto, MultipartFile thumbnail,
		List<MultipartFile> imageFiles) {
		try {
			validateImageCount(createGalleryDto);
			validateImageFileCount(createGalleryDto, imageFiles);
			final Cost cost = determineCost(createGalleryDto);
			validateEndDateForPay(cost, createGalleryDto);
			String thumbnailUrl = s3Service.uploadFile(thumbnail);
			final Gallery gallery = Gallery.create(createGalleryDto, thumbnailUrl, cost);
			galleryRepository.save(gallery);
			saveHashtags(createGalleryDto.hashTags(), gallery);
			saveImages(createGalleryDto.images(), imageFiles, gallery);
		} catch (IOException e) {
			throw new BadRequestException(ErrorCode.FAIL_INVALID_REQUEST);
		}
	}

	public void deleteGallery(DeleteGalleryDto deleteGalleryDto) {
		Gallery gallery = findGalleryById(deleteGalleryDto.galleryId());
		deleteImagesByGallery(gallery);
		deleteThumbnail(gallery);
		deleteHashtagsByGallery(gallery);
		deleteGalleryEntity(gallery);
	}

	private void saveHashtags(List<String> hashTags, Gallery gallery) {
		if (hashTags != null) {
			validateHashtagsSize(hashTags);
			validateHashTagsLength(hashTags);
			final List<Hashtag> hashtags = hashTags.stream()
				.map(tag -> Hashtag.builder().tag(tag).gallery(gallery).build())
				.collect(Collectors.toList());
			hashtagRepository.saveAll(hashtags);
		}
	}

	private void saveImages(List<ImageInfoDto> imageInfoDtos, List<MultipartFile> imageFiles, Gallery gallery) {
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

	private Cost determineCost(CreateGalleryDto createGalleryDto) {
		if (createGalleryDto.fee() == PAYMENT_REQUIRED) {
			return Cost.FREE;
		}
		return Cost.PAY;
	}

	private void validateImageCount(CreateGalleryDto createGalleryDto) {
		if (createGalleryDto.images().size() > MAX_IMAGE_SIZE) {
			throw new BadRequestException(ErrorCode.FAIL_EXHIBITION_ITEM_LIMIT_EXCEEDED);
		}
	}

	private void validateImageFileCount(CreateGalleryDto createGalleryDto, List<MultipartFile> imageFiles) {
		if (createGalleryDto.images().size() != imageFiles.size()) {
			throw new BadRequestException(ErrorCode.FAIL_INVALID_EXHIBITION_ITEM_INFO);
		}
	}

	private void validateEndDateForPay(Cost cost, CreateGalleryDto createGalleryDto) {
		if (cost == Cost.PAY && createGalleryDto.endDate() == null) {
			throw new BadRequestException(ErrorCode.FAIL_INVALID_END_DATE_FOR_PAY);
		}
	}

	private void validateHashtagsSize(List<String> hashTags) {
		if (hashTags.size() > MAX_HASHTAG_SIZE) {
			throw new BadRequestException(ErrorCode.FAIL_HASHTAG_SIZE_EXCEEDED);
		}
	}

	private void validateHashTagsLength(List<String> hashTags) {
		final Pattern pattern = Pattern.compile("^[^\\s]{1,10}$");
		boolean invalidTagFound = hashTags.parallelStream().anyMatch(tag -> !pattern.matcher(tag).matches());
		if (invalidTagFound) {
			throw new BadRequestException(ErrorCode.FAIL_TAG_CONTAINS_SPACE_OR_INVALID_LENGTH);
		}
	}

	private Gallery findGalleryById(Long galleryId) {
		return galleryRepository.findById(galleryId)
			.orElseThrow(() -> new NotFoundException(ErrorCode.FAIL_GALLERY_NOT_FOUND));
	}

	private void deleteImagesByGallery(Gallery gallery) {
		List<Image> images = imageRepository.findByGallery(gallery);
		for (Image image : images) {
			s3Service.deleteFile(image.getImageUri());
			imageRepository.delete(image);
		}
	}

	private void deleteThumbnail(Gallery gallery) {
		s3Service.deleteFile(gallery.getThumbnail());
	}

	private void deleteHashtagsByGallery(Gallery gallery) {
		List<Hashtag> hashtags = hashtagRepository.findByGallery(gallery);
		hashtagRepository.deleteAll(hashtags);
	}

	private void deleteGalleryEntity(Gallery gallery) {
		galleryRepository.delete(gallery);
	}
}
