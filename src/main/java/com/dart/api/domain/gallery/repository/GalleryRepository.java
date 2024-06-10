package com.dart.api.domain.gallery.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.dart.api.domain.gallery.entity.Gallery;

@Repository
public interface GalleryRepository extends JpaRepository<Gallery, Long> {
	Page<Gallery> findAllByIsPaidTrue(Pageable pageable);

	@Query("SELECT g.isPaid FROM Gallery g WHERE g.id = :id")
	boolean findIsPaidById(Long id);
}
