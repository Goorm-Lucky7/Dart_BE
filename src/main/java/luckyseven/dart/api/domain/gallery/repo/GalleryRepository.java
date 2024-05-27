package luckyseven.dart.api.domain.gallery.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import luckyseven.dart.api.domain.gallery.entity.Gallery;

public interface GalleryRepository extends JpaRepository<Gallery, Long> {
}
