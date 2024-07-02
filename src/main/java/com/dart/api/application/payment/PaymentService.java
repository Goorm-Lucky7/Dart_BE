package com.dart.api.application.payment;

import static com.dart.global.common.util.PaymentConstant.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dart.api.domain.auth.entity.AuthUser;
import com.dart.api.domain.gallery.entity.Gallery;
import com.dart.api.domain.gallery.repository.GalleryRepository;
import com.dart.api.domain.member.entity.Member;
import com.dart.api.domain.member.repository.MemberRepository;
import com.dart.api.domain.payment.entity.OrderType;
import com.dart.api.domain.payment.entity.Payment;
import com.dart.api.domain.payment.repository.PaymentRepository;
import com.dart.api.dto.page.PageInfo;
import com.dart.api.dto.page.PageResponse;
import com.dart.api.dto.payment.response.OrderReadDto;
import com.dart.api.dto.payment.response.PaymentReadDto;
import com.dart.global.error.exception.BadRequestException;
import com.dart.global.error.exception.NotFoundException;
import com.dart.global.error.exception.UnauthorizedException;
import com.dart.global.error.model.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentService {
	private final GalleryRepository galleryRepository;
	private final MemberRepository memberRepository;
	private final PaymentRepository paymentRepository;

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

		return gallery.toOrderReadDto(calculateCost(order, gallery));
	}

	private static void validateNotPaymentGallery(String order, Gallery gallery) {
		if (!gallery.isPaid() && OrderType.TICKET.getValue().equals(order)) {
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
		if (OrderType.TICKET.getValue().equals(order)) {
			return gallery.getFee();
		}

		return gallery.getGeneratedCost();
	}

	private static void validateOrder(String order) {
		if (!OrderType.contains(order)) {
			throw new BadRequestException(ErrorCode.FAIL_INVALID_ORDER);
		}
	}
}
