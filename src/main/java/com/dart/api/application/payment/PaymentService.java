package com.dart.api.application.payment;

import static com.dart.global.common.PaymentConstant.*;

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

import com.dart.api.domain.auth.AuthUser;
import com.dart.api.domain.gallery.entity.Gallery;
import com.dart.api.domain.gallery.repo.GalleryRepository;
import com.dart.api.domain.member.entity.Member;
import com.dart.api.domain.member.repo.MemberRepository;
import com.dart.api.domain.payment.entity.Payment;
import com.dart.api.domain.payment.repo.PaymentRepository;
import com.dart.api.dto.page.PageInfo;
import com.dart.api.dto.page.PageResponse;
import com.dart.api.dto.payment.request.PaymentCreateDto;
import com.dart.api.dto.payment.response.PaymentApproveDto;
import com.dart.api.dto.payment.response.PaymentReadDto;
import com.dart.api.dto.payment.response.PaymentReadyDto;
import com.dart.global.config.PaymentProperties;
import com.dart.global.error.exception.NotFoundException;
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
	private PaymentReadyDto paymentReadyDto;

	public PaymentReadyDto ready(PaymentCreateDto dto, AuthUser authUser) {
		final Member member = memberRepository.findByEmail(authUser.email())
			.orElseThrow(() -> new NotFoundException(ErrorCode.FAIL_MEMBER_NOT_FOUND));
		final MultiValueMap<String, String> params = readyToBody(dto, member.getId());
		final HttpHeaders headers = setHeaders();
		final HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);
		final RestTemplate restTemplate = new RestTemplate();

		return paymentReadyDto = restTemplate.postForObject(
			READY_URL,
			requestEntity,
			PaymentReadyDto.class);
	}

	public PaymentApproveDto approve(String token, Long id, String order) {
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

		gallery.pay();
		paymentRepository.save(payment);

		return paymentApproveDto;
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
}
