package luckyseven.dart.api.domain.review.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import luckyseven.dart.api.domain.review.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {
}
