package luckyseven.dart.api.domain.gallery.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import luckyseven.dart.api.domain.gallery.entity.Gallery;
import luckyseven.dart.api.domain.gallery.entity.Hashtag;

public interface HashtagRepository extends JpaRepository<Hashtag, Long> {
	List<Hashtag> findByGallery(Gallery gallery);
}
