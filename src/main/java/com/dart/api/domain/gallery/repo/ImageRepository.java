package com.dart.api.domain.gallery.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dart.api.domain.gallery.entity.Image;

public interface ImageRepository extends JpaRepository<Image, Long> {
}
