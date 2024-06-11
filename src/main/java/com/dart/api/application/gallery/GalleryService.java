package com.dart.api.application.gallery;

import static com.dart.global.common.util.GlobalConstant.*;
import static com.dart.global.common.util.PaymentConstant.*;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.dart.api.application.chat.ChatService;
import com.dart.api.domain.auth.entity.AuthUser;
import com.dart.api.domain.gallery.entity.Cost;
import com.dart.api.domain.gallery.entity.Gallery;
import com.dart.api.domain.gallery.entity.Hashtag;
import com.dart.api.domain.gallery.repository.GalleryRepository;
import com.dart.api.domain.gallery.repository.HashtagRepository;
import com.dart.api.domain.member.entity.Member;
import com.dart.api.domain.member.repository.MemberRepository;
import com.dart.api.domain.payment.entity.Order;
import com.dart.api.domain.payment.repository.PaymentRepository;
import com.dart.api.domain.review.repository.ReviewRepository;
import com.dart.api.dto.gallery.request.CreateGalleryDto;
import com.dart.api.dto.gallery.request.DeleteGalleryDto;
import com.dart.api.dto.gallery.response.GalleryAllResDto;
import com.dart.api.dto.gallery.response.GalleryInfoDto;
import com.dart.api.dto.gallery.response.GalleryReadIdDto;
import com.dart.api.dto.gallery.response.GalleryResDto;
import com.dart.api.dto.gallery.response.ImageResDto;
import com.dart.api.dto.page.PageInfo;
import com.dart.api.dto.page.PageResponse;
import com.dart.api.infrastructure.redis.RedisGalleryRepository;
import com.dart.api.infrastructure.s3.S3Service;
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
	private final ReviewRepository reviewRepository;
	private final PaymentRepository paymentRepository;
	private final ImageService imageService;
	private final S3Service s3Service;
	private final RedisGalleryRepository redisGalleryRepository;
	private final ChatService chatService;

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

			imageService.saveImages(createGalleryDto.informations(), imageFiles, gallery);

			chatService.createChatRoom(gallery);

			waitPayment(gallery);

			return gallery.toReadIdDto();
		} catch (IOException e) {
			throw new BadRequestException(ErrorCode.FAIL_INVALID_REQUEST);
		}
	}

	@Transactional(readOnly = true)
	public PageResponse<GalleryAllResDto> getAllGalleries(int page, int size, String category, String keyword,
		String sort, String cost, String display, AuthUser authUser) {
		final PageRequest pageRequest = PageRequest.of(page, size);

		final Page<Gallery> galleryPage = galleryRepository.findGalleriesByCriteria(pageRequest, category, keyword,
			sort, cost, display);

		final List<GalleryAllResDto> galleries = mapGalleriesToDto(galleryPage.getContent(), authUser);

		final PageInfo pageInfo = new PageInfo(galleryPage.getNumber(), galleryPage.isLast());

		return new PageResponse<>(galleries, pageInfo);
	}

	@Transactional(readOnly = true)
	public GalleryResDto getGallery(Long galleryId, AuthUser authUser) {
		final Gallery gallery = findGalleryById(galleryId);

		boolean hasComment = checkIfUserHasCommented(gallery, authUser);

		List<ImageResDto> images = imageService.getImagesByGalleryId(galleryId);

		return new GalleryResDto(gallery.getTitle(), hasComment, images);
	}

	@Transactional(readOnly = true)
	public GalleryInfoDto getGalleryInfo(Long galleryId, AuthUser authUser) {
		final Gallery gallery = findGalleryById(galleryId);

		final Float reviewAverage = calculateReviewAverage(gallery.getId());

		boolean hasTicket = checkIfUserHasTicket(authUser, gallery);

		final List<String> hashtags = findHashtagsByGallery(gallery);

		return new GalleryInfoDto(gallery.getThumbnail(), gallery.getMember().getNickname(),
			gallery.getMember().getProfileImageUrl(), gallery.getTitle(), gallery.getContent(), gallery.getStartDate(),
			gallery.getEndDate(), gallery.getFee(), reviewAverage, hasTicket, hashtags);
	}

	public void deleteGallery(DeleteGalleryDto deleteGalleryDto, AuthUser authUser) {
		final Member member = findMemberByEmail(authUser.email());
		final Gallery gallery = findGalleryById(deleteGalleryDto.galleryId());

		validateUserOwnership(member, gallery);

		imageService.deleteImagesByGallery(gallery);
		imageService.deleteThumbnail(gallery);
		deleteHashtagsByGallery(gallery);
		deleteGallery(gallery);
	}

	private Member findMemberByEmail(String email) {
		return memberRepository.findByEmail(email)
			.orElseThrow(() -> new UnauthorizedException(ErrorCode.FAIL_LOGIN_REQUIRED));
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
		if (createGalleryDto.informations().size() > MAX_IMAGE_SIZE) {
			throw new BadRequestException(ErrorCode.FAIL_GALLERY_ITEM_LIMIT_EXCEEDED);
		}
	}

	private void validateImageFileCount(CreateGalleryDto createGalleryDto, List<MultipartFile> imageFiles) {
		if (createGalleryDto.informations().size() != imageFiles.size()) {
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

	private List<GalleryAllResDto> mapGalleriesToDto(List<Gallery> galleryList, AuthUser authUser) {
		return galleryList.stream()
			.map(gallery -> mapGalleryToDto(gallery, authUser))
			.toList();
	}

	private GalleryAllResDto mapGalleryToDto(Gallery gallery, AuthUser authUser) {
		final List<String> hashtags = findHashtagsByGallery(gallery);
		return createGalleryAllResDto(gallery, hashtags);
	}

	private GalleryAllResDto createGalleryAllResDto(Gallery gallery, List<String> hashtags) {
		return new GalleryAllResDto(gallery.getId(), gallery.getThumbnail(), gallery.getTitle(), gallery.getStartDate(),
			gallery.getEndDate(), hashtags);
	}

	private Float calculateReviewAverage(Long galleryId) {
		return Optional.ofNullable(reviewRepository.calculateAverageScoreByGalleryId(galleryId))
			.orElse(NO_REVIEW_SCORE);
	}

	private boolean checkIfUserHasTicket(AuthUser authUser, Gallery gallery) {
		if (isAuthUserNull(authUser)) {
			return false;
		}

		final Member member = findMemberByEmail(authUser.email());

		if (isGalleryOwner(gallery, member)) {
			return true;
		}

		return paymentRepository.existsByMemberAndGalleryAndOrder(member, gallery, Order.TICKET);
	}

	private boolean checkIfUserHasCommented(Gallery gallery, AuthUser authUser) {
		if (isAuthUserNull(authUser)) {
			return false;
		}

		Member member = findMemberByEmail(authUser.email());

		if (isGalleryOwner(gallery, member)) {
			return true;
		}

		return hasMemberCommentedOnGallery(member, gallery);
	}

	private boolean isAuthUserNull(AuthUser authUser) {
		return authUser == null;
	}

	private boolean isGalleryOwner(Gallery gallery, Member member) {
		return gallery.getMember().getId().equals(member.getId());
	}

	private boolean hasMemberCommentedOnGallery(Member member, Gallery gallery) {
		return reviewRepository.existsByMemberAndGallery(member, gallery);
	}

	private Gallery findGalleryById(Long galleryId) {
		return galleryRepository.findByIdAndIsPaidTrue(galleryId)
			.orElseThrow(() -> new NotFoundException(ErrorCode.FAIL_GALLERY_NOT_FOUND));
	}

	private List<String> findHashtagsByGallery(Gallery gallery) {
		return hashtagRepository.findTagByGallery(gallery);
	}

	private void deleteHashtagsByGallery(Gallery gallery) {
		List<Hashtag> hashtags = hashtagRepository.findByGallery(gallery);
		hashtagRepository.deleteAll(hashtags);
	}

	private void deleteGallery(Gallery gallery) {
		galleryRepository.delete(gallery);
	}

	private void validateUserOwnership(Member member, Gallery gallery) {
		if (!Objects.equals(member.getId(), gallery.getMember().getId())) {
			throw new BadRequestException(ErrorCode.FAIL_GALLERY_DELETION_FORBIDDEN);
		}
	}

	private void waitPayment(Gallery gallery) {
		if (!gallery.isPaid()) {
			redisGalleryRepository.setGallery(
				gallery.getId().toString(),
				String.valueOf(gallery.getTitle()),
				THIRTY_MINUTE
			);
		}
	}
}
