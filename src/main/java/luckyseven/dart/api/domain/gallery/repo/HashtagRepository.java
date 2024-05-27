package luckyseven.dart.api.domain.gallery.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import luckyseven.dart.api.domain.gallery.entity.Hashtag;

public interface HashtagRepository extends JpaRepository<Hashtag, Long> {
}
