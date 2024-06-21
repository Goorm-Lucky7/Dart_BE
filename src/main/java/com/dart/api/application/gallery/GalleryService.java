package com.dart.api.application.gallery;

import static com.dart.global.common.util.GlobalConstant.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.dart.api.application.chat.ChatRoomService;
import com.dart.api.domain.auth.entity.AuthUser;
import com.dart.api.domain.chat.entity.ChatRoom;
import com.dart.api.domain.chat.repository.ChatRoomRepository;
import com.dart.api.domain.gallery.entity.Cost;
import com.dart.api.domain.gallery.entity.Gallery;
import com.dart.api.domain.gallery.entity.Template;
import com.dart.api.domain.gallery.repository.GalleryRepository;
import com.dart.api.domain.member.entity.Member;
import com.dart.api.domain.member.repository.MemberRepository;
import com.dart.api.domain.payment.entity.Order;
import com.dart.api.domain.payment.repository.PaymentRedisRepository;
import com.dart.api.domain.payment.repository.PaymentRepository;
import com.dart.api.domain.review.repository.ReviewRepository;
import com.dart.api.dto.gallery.request.CreateGalleryDto;
import com.dart.api.dto.gallery.request.DeleteGalleryDto;
import com.dart.api.dto.gallery.response.GalleryAllResDto;
import com.dart.api.dto.gallery.response.GalleryInfoDto;
import com.dart.api.dto.gallery.response.GalleryMypageResDto;
import com.dart.api.dto.gallery.response.GalleryReadIdDto;
import com.dart.api.dto.gallery.response.GalleryResDto;
import com.dart.api.dto.gallery.response.ImageResDto;
import com.dart.api.dto.gallery.response.ReviewGalleryInfoDto;
import com.dart.api.dto.page.PageInfo;
import com.dart.api.dto.page.PageResponse;
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
	private final HashtagService hashtagService;
	private final ReviewRepository reviewRepository;
	private final PaymentRepository paymentRepository;
	private final ImageService imageService;
	private final S3Service s3Service;
	private final PaymentRedisRepository paymentRedisRepository;
	private final ChatRoomService chatRoomService;
	private final ChatRoomRepository chatRoomRepository;

	public GalleryReadIdDto createGallery(CreateGalleryDto createGalleryDto, MultipartFile thumbnail,
		List<MultipartFile> imageFiles, AuthUser authUser) {
		final Member member = findMemberByEmail(authUser.email());

		validateImageCount(createGalleryDto);
		validateImageFileCount(createGalleryDto, imageFiles);

		final Cost cost = determineCost(createGalleryDto);

		validateEndDateForPay(cost, createGalleryDto);
		validateTemplate(createGalleryDto.template());

		String thumbnailUrl = s3Service.uploadFile(thumbnail);

		final Gallery gallery = Gallery.create(createGalleryDto, thumbnailUrl, cost, member);
		galleryRepository.save(gallery);

		hashtagService.saveHashtags(createGalleryDto.hashtags(), gallery);

		imageService.saveImages(createGalleryDto.informations(), imageFiles, gallery);

		chatRoomService.createChatRoom(gallery);

		waitPayment(gallery);

		return gallery.toReadIdDto();
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
	public PageResponse<GalleryMypageResDto> getMypageGalleries(int page, int size, String nickname,
		AuthUser authUser) {
		final PageRequest pageRequest = PageRequest.of(page, size);

		validateRequest(nickname, authUser);
		final Member member = findMember(nickname, authUser);

		Page<Gallery> galleryPage = galleryRepository.findByMemberAndIsPaidTrueOrderByCreatedAtDesc(member,
			pageRequest);
		List<GalleryMypageResDto> galleryDtos = convertToGalleryMypageResDtos(galleryPage);

		PageInfo pageInfo = new PageInfo(galleryPage.getNumber(), galleryPage.isLast());
		return new PageResponse<>(galleryDtos, pageInfo);
	}

	@Transactional(readOnly = true)
	public GalleryResDto getGallery(Long galleryId, AuthUser authUser) {
		final Gallery gallery = findGalleryById(galleryId);

		boolean hasComment = checkIfUserHasCommented(gallery, authUser);

		boolean hasTicket = checkIfUserHasTicket(authUser, gallery);

		List<ImageResDto> images = imageService.getImagesByGalleryId(galleryId);

		final ChatRoom chatRoom = chatRoomRepository.findByGallery(gallery)
			.orElseThrow(() -> new NotFoundException(ErrorCode.FAIL_CHAT_ROOM_NOT_FOUND));

		return new GalleryResDto(
			gallery.getTitle(),
			hasComment,
			gallery.getMember().getNickname(),
			gallery.getTemplate().toString(),
			images,
			chatRoom.getId(),
			hasTicket);

	}

	@Transactional(readOnly = true)
	public GalleryInfoDto getGalleryInfo(Long galleryId, AuthUser authUser) {
		final Gallery gallery = findGalleryById(galleryId);

		final Float reviewAverage = calculateReviewAverage(gallery.getId());

		boolean hasTicket = checkIfUserHasTicket(authUser, gallery);

		final List<String> hashtags = hashtagService.findHashtagsByGallery(gallery);

		boolean isOpen = isGalleryOpen(gallery);

		return new GalleryInfoDto(gallery.getThumbnail(), gallery.getMember().getNickname(),
			gallery.getMember().getProfileImageUrl(), gallery.getTitle(), gallery.getContent(), gallery.getStartDate(),
			gallery.getEndDate(), gallery.getFee(), reviewAverage, hasTicket, isOpen, hashtags);
	}

	@Transactional(readOnly = true)
	public ReviewGalleryInfoDto getReviewGalleryInfo(Long galleryId, AuthUser authUser) {
		final Gallery gallery = findGalleryById(galleryId);
		final Float reviewAverage = calculateReviewAverage(gallery.getId());
		return new ReviewGalleryInfoDto(gallery.getThumbnail(), gallery.getMember().getNickname(),
			gallery.getMember().getProfileImageUrl(), gallery.getTitle(), gallery.getStartDate(), gallery.getEndDate(),
			reviewAverage);
	}

	public void deleteGallery(DeleteGalleryDto deleteGalleryDto, AuthUser authUser) {
		final Member member = findMemberByEmail(authUser.email());
		final Gallery gallery = findGalleryById(deleteGalleryDto.galleryId());

		validateUserOwnership(member, gallery);

		chatRoomService.deleteChatRoom(gallery);

		imageService.deleteImagesByGallery(gallery);
		imageService.deleteThumbnail(gallery);
		hashtagService.deleteHashtagsByGallery(gallery);
		deleteGallery(gallery);
	}

	private Member findMemberByEmail(String email) {
		return memberRepository.findByEmail(email)
			.orElseThrow(() -> new UnauthorizedException(ErrorCode.FAIL_LOGIN_REQUIRED));
	}

	private Member findMemberByNickname(String nickname) {
		return memberRepository.findByNickname(nickname)
			.orElseThrow(() -> new NotFoundException(ErrorCode.FAIL_MEMBER_NOT_FOUND));
	}

	private Cost determineCost(CreateGalleryDto createGalleryDto) {
		if (createGalleryDto.fee() == PAYMENT_REQUIRED) {
			return Cost.FREE;
		}
		return Cost.PAY;
	}

	private void validateTemplate(String template) {
		if (!Template.isValidTemplate(template)) {
			throw new BadRequestException(ErrorCode.FAIL_TEMPLATE_NOT_FOUND);
		}
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

	private List<GalleryAllResDto> mapGalleriesToDto(List<Gallery> galleryList, AuthUser authUser) {
		return galleryList.stream().map(gallery -> mapGalleryToDto(gallery, authUser)).toList();
	}

	private GalleryAllResDto mapGalleryToDto(Gallery gallery, AuthUser authUser) {
		return createGalleryAllResDto(gallery);
	}

	private GalleryAllResDto createGalleryAllResDto(Gallery gallery) {
		return new GalleryAllResDto(gallery.getId(), gallery.getThumbnail(), gallery.getTitle(), gallery.getStartDate(),
			gallery.getEndDate());
	}

	private void validateRequest(String nickname, AuthUser authUser) {
		if ((nickname == null || nickname.isEmpty()) && authUser == null) {
			throw new BadRequestException(ErrorCode.FAIL_NO_TARGET_MEMBER_PROVIDED);
		}
	}

	private Member findMember(String nickname, AuthUser authUser) {
		return Optional.ofNullable(nickname)
			.filter(name -> !name.isEmpty())
			.map(this::findMemberByNickname)
			.orElseGet(() -> findMemberByEmail(authUser.email()));
	}

	private List<GalleryMypageResDto> convertToGalleryMypageResDtos(Page<Gallery> galleryPage) {
		return galleryPage.stream().map(this::convertToGalleryMypageResDto).collect(Collectors.toList());
	}

	private GalleryMypageResDto convertToGalleryMypageResDto(Gallery gallery) {
		List<String> hashtags = hashtagService.findHashtagsByGallery(gallery);

		return new GalleryMypageResDto(gallery.getId(), gallery.getThumbnail(), gallery.getTitle(),
			gallery.getStartDate(), gallery.getEndDate(), gallery.getFee(), hashtags);
	}

	private Float calculateReviewAverage(Long galleryId) {
		return Optional.ofNullable(reviewRepository.calculateAverageScoreByGalleryId(galleryId))
			.orElse(NO_REVIEW_SCORE);
	}

	private boolean checkIfUserHasTicket(AuthUser authUser, Gallery gallery) {
		if (isFreeGallery(gallery)) {
			return true;
		}

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

	private boolean isFreeGallery(Gallery gallery) {
		return gallery.getCost() == Cost.FREE;
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

	private boolean isGalleryOpen(Gallery gallery) {
		LocalDateTime now = LocalDateTime.now();
		return (gallery.getStartDate().isBefore(now) || gallery.getStartDate().isEqual(now)) &&
			(gallery.getEndDate() == null || gallery.getEndDate().isAfter(now) || gallery.getEndDate().isEqual(now));
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
			paymentRedisRepository.setData(
				gallery.getId().toString(),
				String.valueOf(gallery.getTitle())
			);
		}
	}
}
