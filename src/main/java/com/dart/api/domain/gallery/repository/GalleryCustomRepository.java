package com.dart.api.domain.gallery.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.dart.api.domain.gallery.entity.Gallery;

public interface GalleryCustomRepository {
	Page<Gallery> findGalleriesByCriteria(Pageable pageable, String category, String keyword, String sort, String cost,
		String display);
}
