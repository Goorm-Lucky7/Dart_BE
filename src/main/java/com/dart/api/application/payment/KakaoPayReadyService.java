package com.dart.api.application.payment;

import static com.dart.global.common.util.PaymentConstant.*;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.dart.api.domain.auth.entity.AuthUser;
import com.dart.api.domain.coupon.entity.GeneralCouponWallet;
import com.dart.api.domain.coupon.entity.PriorityCouponWallet;
import com.dart.api.domain.coupon.repository.GeneralCouponWalletRepository;
import com.dart.api.domain.coupon.repository.PriorityCouponWalletRepository;
import com.dart.api.domain.gallery.entity.Gallery;
import com.dart.api.domain.gallery.repository.GalleryRepository;
import com.dart.api.domain.member.entity.Member;
import com.dart.api.domain.member.repository.MemberRepository;
import com.dart.api.domain.payment.entity.Order;
import com.dart.api.domain.payment.entity.OrderType;
import com.dart.api.domain.payment.repository.OrderRepository;
import com.dart.api.domain.payment.repository.PaymentRepository;
import com.dart.api.dto.payment.request.PaymentCreateDto;
import com.dart.api.dto.payment.response.PaymentReadyDto;
import com.dart.global.config.PaymentProperties;
import com.dart.global.error.exception.BadRequestException;
import com.dart.global.error.exception.ConflictException;
import com.dart.global.error.exception.NotFoundException;
import com.dart.global.error.model.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class KakaoPayReadyService {
	private final GalleryRepository galleryRepository;
	private final PaymentProperties paymentProperties;
	private final MemberRepository memberRepository;
	private final PaymentRepository paymentRepository;
	private final GeneralCouponWalletRepository generalCouponWalletRepository;
	private final PriorityCouponWalletRepository priorityCouponWalletRepository;
	private final OrderRepository orderRepository;

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
		final PaymentReadyDto paymentReadyDto = restTemplate.postForObject(
			READY_URL,
			requestEntity,
			PaymentReadyDto.class
		);

		validateAlreadyPayment(dto, member);

		final Order order = Order.create(paymentReadyDto.tid(), member.getId(), dto.galleryId());
		orderRepository.save(order);

		return paymentReadyDto;
	}

	public MultiValueMap<String, String> readyToBody(PaymentCreateDto dto, Long memberId) {
		final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		final Gallery gallery = galleryRepository.findById(dto.galleryId())
			.orElseThrow(() -> new NotFoundException(ErrorCode.FAIL_GALLERY_NOT_FOUND));

		params.add("cid", CID);
		params.add("partner_order_id", dto.galleryId().toString());
		params.add("partner_user_id", memberId.toString());
		params.add("item_name", gallery.getTitle());
		params.add("item_code", gallery.getId().toString());
		params.add("quantity", QUANTITY);
		params.add("total_amount", decideCost(dto, gallery, memberId));
		params.add("tax_free_amount", TAX);
		params.add("approval_url",
			SUCCESS_URL + "/" + memberId + "/" + dto.order() + "/" + dto.couponId() + "/" + dto.isPriority() + "/"
				+ dto.galleryId());
		params.add("cancel_url", CANCEL_URL + "/" + memberId + "/" + dto.galleryId());
		params.add("fail_url", FAIL_URL);

		return params;
	}

	public String decideCost(PaymentCreateDto dto, Gallery gallery, Long memberId) {
		if (dto.couponId() == FREE) {
			return calculateWithoutCoupon(dto.order(), gallery);
		}

		if (dto.isPriority()) {
			final PriorityCouponWallet priorityCouponWallet = findPriorityCouponWallet(memberId, dto.couponId());

			return calculateWithCoupon(dto.order(), gallery,
				priorityCouponWallet.getPriorityCoupon().getCouponType().getValue());
		}

		final GeneralCouponWallet generalCouponWallet = findGeneralCouponWallet(memberId, dto.couponId());

		return calculateWithCoupon(dto.order(), gallery,
			generalCouponWallet.getGeneralCoupon().getCouponType().getValue());
	}

	public HttpHeaders setHeaders() {
		final HttpHeaders headers = new HttpHeaders();

		headers.add("Authorization", "KakaoAK " + paymentProperties.getAdminKey());
		headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
		headers.add("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE + ";charset=UTF-8");

		return headers;
	}

	private String calculateWithoutCoupon(String order, Gallery gallery) {
		if (OrderType.TICKET.getValue().equals(order)) {
			return String.valueOf(gallery.getFee());
		}

		return String.valueOf(gallery.getGeneratedCost());
	}

	private String calculateWithCoupon(String order, Gallery gallery, int couponValue) {
		if (OrderType.TICKET.getValue().equals(order)) {
			return String.valueOf((int)((double)(100 - couponValue) / 100 * gallery.getFee()));
		}

		return String.valueOf((int)((double)(100 - couponValue) / 100 * gallery.getGeneratedCost()));
	}

	private PriorityCouponWallet findPriorityCouponWallet(Long memberId, Long couponId) {
		return priorityCouponWalletRepository
			.findByMemberIdAndPriorityCouponIdAndIsUsedFalse(memberId, couponId)
			.orElseThrow(() -> new NotFoundException(ErrorCode.FAIL_COUPON_NOT_FOUND));
	}

	private GeneralCouponWallet findGeneralCouponWallet(Long memberId, Long couponId) {
		return generalCouponWalletRepository
			.findByMemberIdAndGeneralCouponIdAndIsUsedFalse(memberId, couponId)
			.orElseThrow(() -> new NotFoundException(ErrorCode.FAIL_COUPON_NOT_FOUND));
	}

	private void validateAlreadyPayment(PaymentCreateDto dto, Member member) {
		if (orderRepository.existsByMemberIdAndGalleryId(member.getId(), dto.galleryId())) {
			throw new ConflictException(ErrorCode.FAIL_PAYMENT_CONFLICT);
		}
	}

	private static void validateOrder(String order) {
		if (!OrderType.contains(order)) {
			throw new BadRequestException(ErrorCode.FAIL_INVALID_ORDER);
		}
	}

	private void validateExistGallery(PaymentCreateDto dto) {
		if (dto.order().equals(OrderType.TICKET.getValue()) && !galleryRepository.findIsPaidById(dto.galleryId())) {
			throw new BadRequestException(ErrorCode.FAIL_NOT_PAYMENT_GALLERY);
		}
	}

	private void validateAlreadyTicket(PaymentCreateDto dto, Member member) {
		if (dto.order().equals(OrderType.TICKET.getValue()) && paymentRepository.existsByMemberAndGalleryIdAndOrderType(
			member,
			dto.galleryId(), OrderType.TICKET)) {
			throw new BadRequestException(ErrorCode.FAIL_ALREADY_PAID_TICKET);

		}
	}

	private void validateAlreadyGallery(PaymentCreateDto dto, Member member) {
		if (dto.order().equals(OrderType.PAID_GALLERY.getValue())
			&& paymentRepository.existsByMemberAndGalleryIdAndOrderType(
			member, dto.galleryId(), OrderType.PAID_GALLERY)) {
			throw new BadRequestException(ErrorCode.FAIL_ALREADY_PAID_GALLERY);
		}
	}
}
