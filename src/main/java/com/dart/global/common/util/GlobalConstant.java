package com.dart.global.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GlobalConstant {
	public static final String LOCAL_DOMAIN = "localhost";
	public static final String COOKIE_DOMAIN = "dartgallery.site";
	public static final String BLANK = "";
	public static final int MAX_HASHTAG_SIZE = 5;
	public static final int PAYMENT_REQUIRED = 0;
	public static final int MAX_IMAGE_SIZE = 20;
	public static final long DEFAULT_RESULT_COUNT = 0;

	public static final int ONE_STAR = 1;
	public static final int TWO_STAR = 2;
	public static final int THREE_STAR = 3;
	public static final int FOUR_STAR = 4;
	public static final int FIVE_STAR = 5;
	public static final int ZERO_STAR = 0;
	public static final float NO_REVIEW_SCORE = 0.0f;

	public static final int THIRTY_MINUTES = 30 * 60;
	public static final int THIRTY_DAYS = 30 * 24 * 60 * 60;

	public static final int THUMBNAIL_RESIZING_SIZE = 800;

	public static final int ONE_HUNDRED_PERCENT = 100;

	public static final int INCREMENT_BY_ONE = 1;

	public static final int FIRST_SORT = 0;

	public static final int SECOND_SORT = 1;

	public static final String WATERMARK_TEXT = "D'art";
}
