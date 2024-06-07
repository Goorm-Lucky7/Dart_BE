package com.dart.support;

import java.time.LocalDateTime;
import java.util.List;

import com.dart.api.domain.gallery.entity.Cost;
import com.dart.api.domain.gallery.entity.Gallery;
import com.dart.api.dto.gallery.request.CreateGalleryDto;
import com.dart.api.dto.gallery.request.ImageInfoDto;

public class GalleryFixture {

	public static Gallery createGalleryEntity() {
		return Gallery.create(
			createGalleryEntityForCreateGalleryDto(),
			"https://example.com/thumbnail.jpg",
			Cost.FREE,
			MemberFixture.createMemberEntity()
		);
	}

	public static CreateGalleryDto createGalleryEntityForCreateGalleryDto() {
		return CreateGalleryDto.builder()
			.title("D'ART Gallery")
			.content("This is D'ART Gallery")
			.startDate(LocalDateTime.now())
			.endDate(LocalDateTime.now().plusDays(10))
			.fee(1000)
			.hashTags(List.of("happy", "good", "excellent"))
			.images(List.of(
				new ImageInfoDto("image1.jpg", "Image 1"),
				new ImageInfoDto("image2.jpg", "Image 2"))
			)
			.build();
	}
}
