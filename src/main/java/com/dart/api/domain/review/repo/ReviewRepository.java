package com.dart.api.domain.review.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dart.api.domain.gallery.entity.Gallery;
import com.dart.api.domain.review.entity.Review;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

	//겔러리 최근 생성일 순
	Page<Review> findAllByGalleryOrderByCreatedAtDesc(Gallery gallery, Pageable pageable);
}
