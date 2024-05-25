package luckyseven.ddua.api.domain.review.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import luckyseven.ddua.api.domain.review.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {
}
