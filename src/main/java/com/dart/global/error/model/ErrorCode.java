package com.dart.global.error.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum ErrorCode {

	// 400 BAD REQUEST EXCEPTION
	FAIL_INVALID_REQUEST("[❎ ERROR] 잘못된 요청입니다. 입력 형식을 확인해주세요."),
	FAIL_INCORRECT_PASSWORD("[❎ ERROR] 입력하신 비밀번호가 정확하지 않습니다. 다시 시도해 주세요."),
	FAIL_INCORRECT_EMAIL_CODE("[❎ ERROR] 입력하신 인증코드가 정확하지 않습니다. 다시 시도해 주세요."),
	FAIL_TOKEN_EXPIRED_OR_INVALID("[❎ ERROR] 인증 토큰이 유효하지 않습니다. 다시 로그인해 주세요."),
	FAIL_IMAGE_ALREADY_SET("[❎ ERROR] 선택하신 이미지는 이미 기본 프로필 이미지로 설정되어 있습니다. 다른 이미지를 선택해 주세요."),
	FAIL_COMMENT_LENGTH_EXCEEDED("[❎ ERROR] 댓글 작성 길이를 초과하였습니다."),
	FAIL_INVALID_EMAIL_FORMAT("[❎ ERROR] 잘못된 형식의 이메일입니다. 올바른 이메일을 입력해 주세요."),
	FAIL_INVALID_NICKNAME_FORMAT("[❎ ERROR] 잘못된 형식의 닉네임입니다. 닉네임 규칙을 확인해 주세요."),
	FAIL_INVALID_PASSWORD_FORMAT("[❎ ERROR] 잘못된 형식의 비밀번호입니다. 비밀번호 규칙을 확인해 주세요."),
	FAIL_NECESSARY_THUMBNAIL("[❎ ERROR] 썸네일 이미지가 필요합니다."),
	FAIL_HASHTAG_SIZE_EXCEEDED("[❎ ERROR] 해시태그 생성 가능 개수를 초과하였습니다. 해시태그는 최대 5개까지 생성 가능합니다."),
	FAIL_TAG_CONTAINS_SPACE_OR_INVALID_LENGTH("[❎ ERROR] 해시태그는 공백 없이 10자까지 문자로만 생성이 가능합니다. "),
	FAIL_INVALID_END_DATE_FOR_PAY("[❎ ERROR] 유료 전시의 경우 종료일을 입력해야 합니다."),
	FAIL_GALLERY_ITEM_LIMIT_EXCEEDED("[❎ ERROR] 전시 작품은 최대 20개까지 생성 가능합니다."),
	FAIL_INVALID_GALLERY_ITEM_INFO("[❎ ERROR] 전시 작품에 대한 정보를 잘못 입력하셨습니다."),
	FAIL_INVALID_IMAGE_EXTENSION("[❎ ERROR] 전시 이미지는 JPG, JPEG, PNG 형식만 업로드 가능합니다."),
	FAIL_ALREADY_CREATED_REVIEW("[❎ ERROR] 이미 리뷰를 작성하셨습니다."),
	FAIL_NOT_VERIFIED_EMAIL("[❎ ERROR] 이메일 중복확인을 해주시길 바랍니다."),
	FAIL_NOT_VERIFIED_NICKNAME("[❎ ERROR] 닉네임 중복확인을 해주시길 바랍니다."),
	FAIL_NOT_PAYMENT_GALLERY("[❎ ERROR] 미결제 전시관입니다."),
	FAIL_INVALID_ORDER("[❎ ERROR] 잘못된 형식의 주문입니다."),
	FAIL_ALREADY_PAID_GALLERY("[❎ ERROR] 이미 전시회를 구매하셨습니다."),
	FAIL_ALREADY_PAID_TICKET("[❎ ERROR] 이미 티켓을 구매하셨습니다."),
	FAIL_INVALID_DISPLAY_VALUE("[❎ ERROR] 다음 중 하나의 값을 입력해 주세요: all, upcoming, inprogress, finished"),
	FAIL_INVALID_SORT_VALUE("[❎ ERROR] 다음 중 하나의 값을 입력해 주세요: latest, liked"),
	FAIL_INVALID_COST_VALUE("[❎ ERROR] 다음 중 하나의 값을 입력해 주세요: all, pay, free"),
	FAIL_INVALID_CATEGORY_VALUE("[❎ ERROR] 다음 중 하나의 값을 입력해 주세요: hashtag, author, title"),
	FAIL_INVALID_SESSION_ID("[❎ ERROR] 요청하신 채팅방에 유효한 세션 ID가 필요합니다."),
	FAIL_INVALID_DESTINATION("[❎ ERROR] 요청하신 채팅방의 대상이 유효하지 않습니다."),
	FAIL_CREATED_OWN_REVIEW("[❎ ERROR] 본인의 전시관에 리뷰를 작성하실 수 없습니다."),
	FAIL_CREATED_UNPAID_REVIEW("[❎ ERROR] 미결제 전시관에 리뷰를 작성하실 수 없습니다."),
	FAIL_ALREADY_VERIFIED_EMAIL("[❎ ERROR] 이미 이메일 중복확인이 완료되었습니다."),
	FAIL_NO_TARGET_MEMBER_PROVIDED("[❎ ERROR] 조회하고 싶은 대상을 입력해 주세요."),
	FAIL_INVALID_STOCK_END("[❎ ERROR] 쿠폰 재고가 마감되었습니다."),

	// 401 Unauthorized
	FAIL_LOGIN_REQUIRED("[❎ ERROR] 로그인이 필요한 기능입니다."),
	FAIL_TOKEN_EXPIRED("[❎ ERROR] 인증 토큰이 만료되었습니다. 다시 로그인해 주세요."),
	FAIL_INVALID_TOKEN("[❎ ERROR] 유효하지 않은 인증 토큰입니다. 다시 로그인해 주세요."),
	FAIL_INVALID_EMAIL_CODE("[❎ ERROR] 인증 코드가 만료되었습니다. 이메일 중복확인을 다시 시도해 주세요."),

	// 403 Forbidden
	FAIL_GALLERY_CREATION_FORBIDDEN("[❎ ERROR] 전시 생성 권한이 없습니다."),
	FAIL_COMMENT_CREATION_FORBIDDEN("[❎ ERROR] 댓글 작성 권한이 없습니다."),
	FAIL_GALLERY_DELETION_FORBIDDEN("[❎ ERROR] 전시 삭제 권한이 없습니다."),
	FAIL_COMMENT_DELETION_FORBIDDEN("[❎ ERROR] 댓글 삭제 권한이 없습니다."),

	// 404 Not Found
	FAIL_MEMBER_NOT_FOUND("[❎ ERROR] 요청하신 회원을 찾을 수 없습니다."),
	FAIL_IMAGE_NOT_FOUND("[❎ ERROR] 요청하신 이미지를 찾을 수 없습니다."),
	FAIL_GALLERY_NOT_FOUND("[❎ ERROR] 요청하신 전시관을 찾을 수 없습니다."),
	FAIL_USER_NOT_FOUND("[❎ ERROR] 요청하신 회원을 찾을 수 없습니다."),
	FAIL_COMMENT_NOT_FOUND("[❎ ERROR] 요청하신 댓글을 찾을 수 없습니다."),
	FAIL_TOKEN_NOT_FOUND("[❎ ERROR] 요청하신 토큰을 찾을 수 없습니다."),
	FAIL_REGISTRATION_NOT_FOUND("[❎ ERROR] 요청하신 서비스 제공자를 찾을 수 없습니다."),
	FAIL_CHAT_ROOM_NOT_FOUND("[❎ ERROR] 요청하신 채팅방을 찾을 수 없습니다."),
	FAIL_COUPON_NOT_FOUND("[❎ ERROR] 요청하신 쿠폰을 찾을 수 없습니다."),
	FAIL_TEMPLATE_NOT_FOUND("[❎ ERROR] 요청하신 템플릿을 찾을 수 없습니다."),
	FAIL_NOTIFICATION_NOT_FOUND("[❎ ERROR] 요청하신 알림을 찾을 수 없습니다."),

	// 409 Conflict
	FAIL_EMAIL_CONFLICT("[❎ ERROR] 이미 존재하는 이메일입니다."),
	FAIL_NICKNAME_CONFLICT("[❎ ERROR] 이미 존재하는 닉네임입니다."),
	FAIL_GALLERY_CONFLICT_ALREADY_ENDED("[❎ ERROR] 해당 전시회는 이미 종료된 전시회입니다."),
	FAIL_COUPON_CONFLICT("[❎ ERROR] 이미 쿠폰을 발급 받으셨습니다."),

	// 500 Server Error
	FAIL_INTERNAL_SERVER_ERROR("[❎ ERROR] 서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요."),
	FAIL_EMAIL_SEND("[❎ ERROR] 이메일 전송에 실패했습니다. 잠시 후 다시 시도해주세요.");

	private String message;
}
