package luckyseven.ddua.api.domain.gallery.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import luckyseven.ddua.api.domain.gallery.entity.Gallery;
import luckyseven.ddua.api.domain.gallery.entity.Hashtag;

public interface HashtagRepository extends JpaRepository<Hashtag, Long> {
}
