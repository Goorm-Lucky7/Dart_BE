package com.dart.api.domain.review.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dart.api.domain.review.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {
}
