package com.dart.api.application.review;

import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dart.api.domain.auth.entity.AuthUser;
import com.dart.api.domain.gallery.entity.Gallery;
import com.dart.api.domain.gallery.repository.GalleryRepository;
import com.dart.api.domain.member.entity.Member;
import com.dart.api.domain.member.repository.MemberRepository;
import com.dart.api.domain.review.entity.Review;
import com.dart.api.domain.review.repository.ReviewRepository;
import com.dart.api.dto.review.request.ReviewCreateDto;
import com.dart.support.AuthFixture;
import com.dart.support.GalleryFixture;
import com.dart.support.MemberFixture;

@ExtendWith({MockitoExtension.class})
class ReviewServiceTest {
	@Mock
	private ReviewRepository reviewRepository;

	@Mock
	private GalleryRepository galleryRepository;

	@Mock
	private MemberRepository memberRepository;

	@InjectMocks
	private ReviewService reviewService;

	@Test
	@DisplayName("리뷰를 성공적으로 작성한다. - Void")
	void create_success() {
		// GIVEN
		Member member = MemberFixture.createMemberEntity();
		Gallery gallery = GalleryFixture.createGalleryEntity();
		ReviewCreateDto reviewCreateDto = new ReviewCreateDto(1L, "test", 1);
		AuthUser authUser = AuthFixture.createAuthUserEntity();

		given(memberRepository.findById(any(Long.class))).willReturn(Optional.ofNullable(member));
		given(galleryRepository.findById(any(Long.class))).willReturn(Optional.ofNullable(gallery));
		given(reviewRepository.existsByMemberAndGallery(any(Member.class), any(Gallery.class))).willReturn(false);

		// WHEN
		reviewService.create(reviewCreateDto, authUser);

		// THEN
		verify(reviewRepository, times(1)).save(any(Review.class));
	}
}
