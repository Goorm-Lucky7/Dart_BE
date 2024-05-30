package luckyseven.dart.api.domain.gallery.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import luckyseven.dart.api.domain.gallery.entity.Gallery;
import luckyseven.dart.api.domain.gallery.entity.Image;

public interface ImageRepository extends JpaRepository<Image, Long> {
	List<Image> findByGallery(Gallery gallery);
}
