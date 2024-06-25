package com.dart.api.application.payment;

import static com.dart.global.common.util.PaymentConstant.*;

import java.time.LocalDateTime;

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
import com.dart.api.domain.payment.entity.Payment;
import com.dart.api.domain.payment.repository.PaymentRedisRepository;
import com.dart.api.domain.payment.repository.PaymentRepository;
import com.dart.api.dto.payment.request.PaymentCreateDto;
import com.dart.api.dto.payment.response.PaymentApproveDto;
import com.dart.api.dto.payment.response.PaymentReadyDto;
import com.dart.global.config.PaymentProperties;
import com.dart.global.error.exception.BadRequestException;
import com.dart.global.error.exception.NotFoundException;
import com.dart.global.error.model.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class KakaoPayService {
	private final GalleryRepository galleryRepository;
	private final PaymentProperties paymentProperties;
	private final MemberRepository memberRepository;
	private final PaymentRedisRepository paymentRedisRepository;
	private final PaymentRepository paymentRepository;
	private final GeneralCouponWalletRepository generalCouponWalletRepository;
	private final PriorityCouponWalletRepository priorityCouponWalletRepository;
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

	public String approve(String token, Long id, String order, Long couponId, boolean isPriority) {
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
		final Payment payment = Payment.create(member, gallery, paymentApproveDto.amount().total(), approveAt, order);

		payGallery(order, gallery);
		useCoupon(couponId, isPriority, member);
		paymentRepository.save(payment);

		return gallery.getId().toString();
	}

	public MultiValueMap<String, String> readyToBody(PaymentCreateDto dto, Long memberId) {
		final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		final Gallery gallery = galleryRepository.findById(dto.galleryId())
			.orElseThrow(() -> new NotFoundException(ErrorCode.FAIL_GALLERY_NOT_FOUND));

		params.add("cid", CID);
		params.add("partner_order_id", PARTNER_ORDER);
		params.add("partner_user_id", PARTNER_USER);
		params.add("item_name", String.valueOf(gallery.getTitle()));
		params.add("item_code", gallery.getId().toString());
		params.add("quantity", QUANTITY);
		params.add("total_amount", decideCost(dto, gallery, memberId));
		params.add("tax_free_amount", TAX);
		params.add("approval_url",
			SUCCESS_URL + "/" + memberId + "/" + dto.order() + "/" + dto.couponId() + "/" + dto.isPriority());
		params.add("cancel_url", CANCEL_URL);
		params.add("fail_url", FAIL_URL);

		return params;
	}

	public MultiValueMap<String, String> approveToBody(String token) {
		final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

		params.add("cid", CID);
		params.add("tid", paymentReadyDto.tid());
		params.add("partner_order_id", PARTNER_ORDER);
		params.add("partner_user_id", PARTNER_USER);
		params.add("pg_token", token);

		return params;
	}

	public HttpHeaders setHeaders() {
		final HttpHeaders headers = new HttpHeaders();

		headers.add("Authorization", "KakaoAK " + paymentProperties.getAdminKey());
		headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
		headers.add("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE + ";charset=UTF-8");

		return headers;
	}

	private void useCoupon(Long couponId, boolean isPriority, Member member) {
		if (couponId != FREE) {
			if (isPriority) {
				final PriorityCouponWallet priorityCouponWallet = findPriorityCouponWallet(member.getId(), couponId);
				priorityCouponWallet.use();

				return;
			}

			final GeneralCouponWallet generalCouponWallet = findGeneralCouponWallet(member.getId(), couponId);
			generalCouponWallet.use();
		}
	}

	public String decideCost(PaymentCreateDto dto, Gallery gallery, Long memberId) {
		if (dto.couponId() == FREE) {
			return calculateWithoutCoupon(dto.order(), gallery);
		}

		if (dto.isPriority()) {
			final PriorityCouponWallet priorityCouponWallet = findPriorityCouponWallet(dto.couponId(), memberId);

			return calculateWithCoupon(dto.order(), gallery,
				priorityCouponWallet.getPriorityCoupon().getCouponType().getValue());
		}

		final GeneralCouponWallet generalCouponWallet = findGeneralCouponWallet(dto.couponId(), memberId);

		return calculateWithCoupon(dto.order(), gallery,
			generalCouponWallet.getGeneralCoupon().getCouponType().getValue());
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

	private void payGallery(String order, Gallery gallery) {
		if (order.equals(Order.PAID_GALLERY.getValue())) {
			gallery.pay();
			paymentRedisRepository.deleteData(gallery.getId().toString());
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

	private String calculateWithoutCoupon(String order, Gallery gallery) {
		if (Order.TICKET.getValue().equals(order)) {
			return String.valueOf(gallery.getFee());
		}

		return String.valueOf(gallery.getGeneratedCost());
	}

	private String calculateWithCoupon(String order, Gallery gallery, int couponValue) {
		if (Order.TICKET.getValue().equals(order)) {
			return String.valueOf((int)((double)(100 - couponValue) / 100 * gallery.getFee()));
		}

		return String.valueOf((int)((double)(100 - couponValue) / 100 * gallery.getGeneratedCost()));
	}

	private PriorityCouponWallet findPriorityCouponWallet(Long couponId, Long memberId) {
		return priorityCouponWalletRepository
			.findByIdAndMemberIdAndIsUsedFalse(couponId, memberId)
			.orElseThrow(() -> new NotFoundException(ErrorCode.FAIL_COUPON_NOT_FOUND));
	}

	private GeneralCouponWallet findGeneralCouponWallet(Long couponId, Long memberId) {
		return generalCouponWalletRepository
			.findByIdAndMemberIdAndIsUsedFalse(couponId, memberId)
			.orElseThrow(() -> new NotFoundException(ErrorCode.FAIL_COUPON_NOT_FOUND));
	}
}
