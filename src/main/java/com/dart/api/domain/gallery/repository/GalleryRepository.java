package com.dart.api.domain.gallery.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.dart.api.domain.gallery.entity.Gallery;

@Repository
public interface GalleryRepository extends JpaRepository<Gallery, Long>, GalleryCustomRepository {
	@Query("SELECT g.isPaid FROM Gallery g WHERE g.id = :id")
	boolean findIsPaidById(Long id);

	Optional<Gallery> findByIdAndIsPaidTrue(Long id);

}
