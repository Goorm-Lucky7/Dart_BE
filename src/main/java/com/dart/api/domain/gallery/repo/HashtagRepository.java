package com.dart.api.domain.gallery.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dart.api.domain.gallery.entity.Hashtag;

public interface HashtagRepository extends JpaRepository<Hashtag, Long> {
}
