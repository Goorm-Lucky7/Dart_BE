package com.dart.api.domain.review.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dart.api.domain.gallery.entity.Gallery;
import com.dart.api.domain.member.entity.Member;
import com.dart.api.domain.review.entity.Review;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long>, ReviewRepositoryCustom {
	Page<Review> findAllByGalleryOrderByCreatedAtDesc(Gallery gallery, Pageable pageable);

	boolean existsByMemberAndGallery(Member member, Gallery gallery);
}
