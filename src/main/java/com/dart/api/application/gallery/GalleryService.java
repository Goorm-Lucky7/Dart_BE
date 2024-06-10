package com.dart.api.application.gallery;

import static com.dart.global.common.util.GlobalConstant.*;
import static com.dart.global.common.util.PaymentConstant.*;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.dart.api.domain.auth.entity.AuthUser;
import com.dart.api.domain.gallery.entity.Cost;
import com.dart.api.domain.gallery.entity.Gallery;
import com.dart.api.domain.gallery.entity.Hashtag;
import com.dart.api.domain.gallery.repository.GalleryRepository;
import com.dart.api.domain.gallery.repository.HashtagRepository;
import com.dart.api.domain.member.entity.Member;
import com.dart.api.domain.member.repository.MemberRepository;
import com.dart.api.dto.gallery.request.CreateGalleryDto;
import com.dart.api.dto.gallery.request.DeleteGalleryDto;
import com.dart.api.dto.gallery.response.GalleryReadIdDto;
import com.dart.global.common.util.RedisUtil;
import com.dart.global.common.util.S3Service;
import com.dart.global.error.exception.BadRequestException;
import com.dart.global.error.exception.NotFoundException;
import com.dart.global.error.exception.UnauthorizedException;
import com.dart.global.error.model.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class GalleryService {

	private final MemberRepository memberRepository;
	private final GalleryRepository galleryRepository;
	private final HashtagRepository hashtagRepository;
	private final ImageService imageService;
	private final S3Service s3Service;
	private final RedisUtil redisUtil;

	public GalleryReadIdDto createGallery(CreateGalleryDto createGalleryDto, MultipartFile thumbnail,
		List<MultipartFile> imageFiles, AuthUser authUser) {
		final Member member = findMemberByEmail(authUser.email());
		try {
			validateImageCount(createGalleryDto);
			validateImageFileCount(createGalleryDto, imageFiles);
			final Cost cost = determineCost(createGalleryDto);
			validateEndDateForPay(cost, createGalleryDto);
			String thumbnailUrl = s3Service.uploadFile(thumbnail);
			final Gallery gallery = Gallery.create(createGalleryDto, thumbnailUrl, cost, member);
			galleryRepository.save(gallery);
			saveHashtags(createGalleryDto.hashTags(), gallery);
			imageService.saveImages(createGalleryDto.images(), imageFiles, gallery);
			waitPayment(gallery);

			return gallery.toReadIdDto();
		} catch (IOException e) {
			throw new BadRequestException(ErrorCode.FAIL_INVALID_REQUEST);
		}
	}

	public void deleteGallery(DeleteGalleryDto deleteGalleryDto, AuthUser authUser) {
		final Member member = findMemberByEmail(authUser.email());
		Gallery gallery = findGalleryById(deleteGalleryDto.galleryId());
		validateUserOwnership(member, gallery);
		imageService.deleteImagesByGallery(gallery);
		imageService.deleteThumbnail(gallery);
		deleteHashtagsByGallery(gallery);
		deleteGallery(gallery);
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

	private Cost determineCost(CreateGalleryDto createGalleryDto) {
		if (createGalleryDto.fee() == PAYMENT_REQUIRED) {
			return Cost.FREE;
		}
		return Cost.PAY;
	}

	private void validateImageCount(CreateGalleryDto createGalleryDto) {
		if (createGalleryDto.images().size() > MAX_IMAGE_SIZE) {
			throw new BadRequestException(ErrorCode.FAIL_GALLERY_ITEM_LIMIT_EXCEEDED);
		}
	}

	private void validateImageFileCount(CreateGalleryDto createGalleryDto, List<MultipartFile> imageFiles) {
		if (createGalleryDto.images().size() != imageFiles.size()) {
			throw new BadRequestException(ErrorCode.FAIL_INVALID_GALLERY_ITEM_INFO);
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

	private void deleteHashtagsByGallery(Gallery gallery) {
		List<Hashtag> hashtags = hashtagRepository.findByGallery(gallery);
		hashtagRepository.deleteAll(hashtags);
	}

	private void deleteGallery(Gallery gallery) {
		galleryRepository.delete(gallery);
	}

	private Member findMemberByEmail(String email) {
		return memberRepository.findByEmail(email)
			.orElseThrow(() -> new UnauthorizedException(ErrorCode.FAIL_LOGIN_REQUIRED));
	}

	private void validateUserOwnership(Member member, Gallery gallery) {
		if (!Objects.equals(member.getId(), gallery.getMember().getId())) {
			throw new BadRequestException(ErrorCode.FAIL_GALLERY_DELETION_FORBIDDEN);
		}
	}

	private void waitPayment(Gallery gallery) {
		if (!gallery.isPaid()) {
			redisUtil.setDataExpire(
				gallery.getId().toString(),
				String.valueOf(gallery.getTitle()),
				THIRTY_MINUTE
			);
		}
	}
}
