package com.dart.api.domain.gallery.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dart.api.domain.gallery.entity.Gallery;
import com.dart.api.domain.gallery.entity.Image;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
	List<Image> findByGallery(Gallery gallery);

	List<Image> findByGalleryId(Long galleryId);
}
