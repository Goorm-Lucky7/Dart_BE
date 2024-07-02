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
import com.dart.api.domain.payment.entity.Payment;
import com.dart.api.domain.payment.repository.OrderRepository;
import com.dart.api.domain.payment.repository.PaymentRedisRepository;
import com.dart.api.domain.payment.repository.PaymentRepository;
import com.dart.api.dto.payment.response.PaymentApproveDto;
import com.dart.global.config.PaymentProperties;
import com.dart.global.error.exception.NotFoundException;
import com.dart.global.error.model.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class KakaoPayApproveService {
	private final GalleryRepository galleryRepository;
	private final PaymentProperties paymentProperties;
	private final MemberRepository memberRepository;
	private final PaymentRedisRepository paymentRedisRepository;
	private final PaymentRepository paymentRepository;
	private final GeneralCouponWalletRepository generalCouponWalletRepository;
	private final PriorityCouponWalletRepository priorityCouponWalletRepository;
	private final OrderRepository orderRepository;

	public String approve(String token, Long memberId, String orderType, Long couponId, boolean isPriority,
		Long galleryId) {
		final Order order = orderRepository.findByMemberIdAndGalleryId(memberId, galleryId)
			.orElseThrow(() -> new NotFoundException(ErrorCode.FAIL_ORDER_NOT_FOUND));
		final MultiValueMap<String, String> params = approveToBody(token, order);
		final HttpHeaders headers = setHeaders();
		final RestTemplate restTemplate = new RestTemplate();
		final HttpEntity<MultiValueMap<String, String>> body = new HttpEntity<>(params, headers);
		final PaymentApproveDto paymentApproveDto = restTemplate.postForObject(
			APPROVE_URL,
			body,
			PaymentApproveDto.class);
		final Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new NotFoundException(ErrorCode.FAIL_MEMBER_NOT_FOUND));
		final Gallery gallery = galleryRepository.findById(order.getGalleryId())
			.orElseThrow(() -> new NotFoundException(ErrorCode.FAIL_GALLERY_NOT_FOUND));
		final LocalDateTime approveAt = paymentApproveDto.approved_at();
		final Payment payment = Payment.create(member, gallery, paymentApproveDto.amount().total(), approveAt,
			orderType);

		order.approve();
		payGallery(orderType, gallery);
		useCoupon(couponId, isPriority, member);
		paymentRepository.save(payment);

		return gallery.getId().toString();
	}

	public MultiValueMap<String, String> approveToBody(String token, Order order) {
		final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

		params.add("cid", CID);
		params.add("tid", order.getTid());
		params.add("partner_order_id", order.getGalleryId().toString());
		params.add("partner_user_id", order.getMemberId().toString());
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

	private void payGallery(String orderType, Gallery gallery) {
		if (orderType.equals(OrderType.PAID_GALLERY.getValue())) {
			gallery.pay();
			paymentRedisRepository.deleteData(gallery.getId().toString());
		}
	}
}
