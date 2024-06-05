package com.dart.api.domain.gallery.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dart.api.domain.gallery.entity.Gallery;
import com.dart.api.domain.gallery.entity.Hashtag;

@Repository
public interface HashtagRepository extends JpaRepository<Hashtag, Long> {
	List<Hashtag> findByGallery(Gallery gallery);

	@Query("SELECT h.tag FROM Hashtag h WHERE h.gallery = :gallery")
	List<String> findTagByGallery(@Param("gallery") Gallery gallery);
}
