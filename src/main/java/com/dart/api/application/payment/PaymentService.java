package com.dart.api.application.payment;

import static com.dart.global.common.util.PaymentConstant.*;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.dart.api.domain.auth.entity.AuthUser;
import com.dart.api.domain.gallery.entity.Gallery;
import com.dart.api.domain.gallery.repository.GalleryRepository;
import com.dart.api.domain.member.entity.Member;
import com.dart.api.domain.member.repository.MemberRepository;
import com.dart.api.domain.payment.entity.Order;
import com.dart.api.domain.payment.entity.Payment;
import com.dart.api.domain.payment.repository.PaymentRedisRepository;
import com.dart.api.domain.payment.repository.PaymentRepository;
import com.dart.api.dto.page.PageInfo;
import com.dart.api.dto.page.PageResponse;
import com.dart.api.dto.payment.request.PaymentCreateDto;
import com.dart.api.dto.payment.response.OrderReadDto;
import com.dart.api.dto.payment.response.PaymentApproveDto;
import com.dart.api.dto.payment.response.PaymentReadDto;
import com.dart.api.dto.payment.response.PaymentReadyDto;
import com.dart.global.config.PaymentProperties;
import com.dart.global.error.exception.BadRequestException;
import com.dart.global.error.exception.NotFoundException;
import com.dart.global.error.exception.UnauthorizedException;
import com.dart.global.error.model.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {
	private final GalleryRepository galleryRepository;
	private final MemberRepository memberRepository;
	private final PaymentRepository paymentRepository;
	private final PaymentProperties paymentProperties;
	private final PaymentRedisRepository paymentRedisRepository;

	private PaymentReadyDto paymentReadyDto;

	public PaymentReadyDto ready(PaymentCreateDto dto, AuthUser authUser) {
		validateExistGallery(dto);
		validateOrder(dto.order());

		final Member member = memberRepository.findByEmail(authUser.email())
			.orElseThrow(() -> new NotFoundException(ErrorCode.FAIL_MEMBER_NOT_FOUND));

		validateAlreadyGallery(dto, member);
		validateAlreadyTicket(dto, member);

		final MultiValueMap<String, String> params = readyToBody(dto, member.getId());
		final HttpHeaders headers = setHeaders();
		final HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);
		final RestTemplate restTemplate = new RestTemplate();

		return paymentReadyDto = restTemplate.postForObject(
			READY_URL,
			requestEntity,
			PaymentReadyDto.class);
	}

	public String approve(String token, Long id, String order) {
		final MultiValueMap<String, String> params = approveToBody(token);
		final HttpHeaders headers = setHeaders();
		final RestTemplate restTemplate = new RestTemplate();
		final HttpEntity<MultiValueMap<String, String>> body = new HttpEntity<>(params, headers);
		final PaymentApproveDto paymentApproveDto = restTemplate.postForObject(
			APPROVE_URL,
			body,
			PaymentApproveDto.class);
		final Member member = memberRepository.findById(id)
			.orElseThrow(() -> new NotFoundException(ErrorCode.FAIL_MEMBER_NOT_FOUND));
		final Gallery gallery = galleryRepository.findById(Long.parseLong(paymentApproveDto.item_code()))
			.orElseThrow(() -> new NotFoundException(ErrorCode.FAIL_GALLERY_NOT_FOUND));
		final LocalDateTime approveAt = paymentApproveDto.approved_at();
		final Payment payment = Payment.create(member, gallery, approveAt, order);

		payGallery(order, gallery);
		paymentRepository.save(payment);

		return gallery.getId().toString();
	}

	@Transactional(readOnly = true)
	public PageResponse<PaymentReadDto> readAll(AuthUser authUser, int page, int size) {
		final Member member = memberRepository.findByEmail(authUser.email())
			.orElseThrow(() -> new NotFoundException(ErrorCode.FAIL_MEMBER_NOT_FOUND));
		final Pageable pageable = PageRequest.of(page, size);
		final Page<Payment> payments = paymentRepository.findAllByMemberOrderByApprovedAtDesc(member, pageable);
		final PageInfo pageInfo = new PageInfo(payments.getNumber(), payments.isLast());

		return new PageResponse<>(payments.map(Payment::toReadDto).toList(), pageInfo);
	}

	@Transactional(readOnly = true)
	public OrderReadDto readOrder(Long galleryId, String order, AuthUser authUser) {
		validateLogin(authUser);
		validateOrder(order);

		final Gallery gallery = galleryRepository.findById(galleryId)
			.orElseThrow(() -> new NotFoundException(ErrorCode.FAIL_GALLERY_NOT_FOUND));

		validateNotPaymentGallery(order, gallery);
		validateFree(gallery);

		return OrderReadDto.builder()
			.title(gallery.getTitle())
			.thumbnail(gallery.getThumbnail())
			.nickname(gallery.getMember().getNickname())
			.profileImage(gallery.getMember().getProfileImageUrl())
			.cost(calculateCost(order, gallery))
			.build();
	}

	private MultiValueMap<String, String> readyToBody(PaymentCreateDto dto, Long memberId) {
		final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		final Gallery gallery = galleryRepository.findById(dto.galleryId())
			.orElseThrow(() -> new NotFoundException(ErrorCode.FAIL_GALLERY_NOT_FOUND));

		params.add("cid", CID);
		params.add("partner_order_id", PARTNER_ORDER);
		params.add("partner_user_id", PARTNER_USER);
		params.add("item_name", gallery.getTitle());
		params.add("item_code", gallery.getId().toString());
		params.add("quantity", QUANTITY);
		params.add("total_amount", String.valueOf(gallery.getFee()));
		params.add("tax_free_amount", TAX);
		params.add("approval_url", SUCCESS_URL + "/" + memberId + "/" + dto.order());
		params.add("cancel_url", CANCEL_URL);
		params.add("fail_url", FAIL_URL);

		return params;
	}

	private MultiValueMap<String, String> approveToBody(String token) {
		final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

		params.add("cid", CID);
		params.add("tid", paymentReadyDto.tid());
		params.add("partner_order_id", PARTNER_ORDER);
		params.add("partner_user_id", PARTNER_USER);
		params.add("pg_token", token);

		return params;
	}

	private HttpHeaders setHeaders() {
		final HttpHeaders headers = new HttpHeaders();

		headers.add("Authorization", "KakaoAK " + paymentProperties.getAdminKey());
		headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
		headers.add("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE + ";charset=UTF-8");

		return headers;
	}

	private static void validateNotPaymentGallery(String order, Gallery gallery) {
		if (!gallery.isPaid() && Order.TICKET.getValue().equals(order)) {
			throw new BadRequestException(ErrorCode.FAIL_NOT_PAYMENT_GALLERY);
		}
	}

	private static void validateLogin(AuthUser authUser) {
		if (authUser == null) {
			throw new UnauthorizedException(ErrorCode.FAIL_LOGIN_REQUIRED);
		}
	}

	private void validateFree(Gallery gallery) {
		if (gallery.getGeneratedCost() == FREE && gallery.getFee() == FREE) {
			throw new BadRequestException(ErrorCode.FAIL_INVALID_ORDER);
		}
	}

	private int calculateCost(String order, Gallery gallery) {
		if (Order.TICKET.getValue().equals(order)) {
			return gallery.getFee();
		}

		return gallery.getGeneratedCost();
	}

	private void validateAlreadyTicket(PaymentCreateDto dto, Member member) {
		if (dto.order().equals(Order.TICKET.getValue()) && paymentRepository.existsByMemberAndGalleryIdAndOrder(member,
			dto.galleryId(), Order.TICKET)) {
			throw new BadRequestException(ErrorCode.FAIL_ALREADY_PAID_TICKET);

		}
	}

	private void validateAlreadyGallery(PaymentCreateDto dto, Member member) {
		if (dto.order().equals(Order.PAID_GALLERY.getValue()) && paymentRepository.existsByMemberAndGalleryIdAndOrder(
			member, dto.galleryId(), Order.PAID_GALLERY)) {
			throw new BadRequestException(ErrorCode.FAIL_ALREADY_PAID_GALLERY);
		}
	}

	private static void validateOrder(String order) {
		if (!Order.contains(order)) {
			throw new BadRequestException(ErrorCode.FAIL_INVALID_ORDER);
		}
	}

	private void validateExistGallery(PaymentCreateDto dto) {
		if (dto.order().equals(Order.TICKET.getValue()) && !galleryRepository.findIsPaidById(dto.galleryId())) {
			throw new BadRequestException(ErrorCode.FAIL_NOT_PAYMENT_GALLERY);
		}
	}

	private void payGallery(String order, Gallery gallery) {
		if (order.equals(Order.PAID_GALLERY.getValue())) {
			gallery.pay();
			paymentRedisRepository.deleteData(gallery.getId().toString());
		}
	}
}
