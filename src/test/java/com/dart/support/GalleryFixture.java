package com.dart.support;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.dart.api.domain.gallery.entity.Cost;
import com.dart.api.domain.gallery.entity.Gallery;
import com.dart.api.domain.member.entity.Member;
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

	public static Gallery createGalleryEntityForAuthor() {
		return Gallery.create(
			createGalleryEntityForCreateGalleryDto(),
			"https://example.com/thumbnail.jpg",
			Cost.FREE,
			MemberFixture.createMemberEntityForAuthor()
		);
	}

	public static Gallery createGalleryEntity(Member member) {
		return Gallery.builder()
			.title("D'ART Gallery")
			.content("This is D'ART Gallery")
			.thumbnail("https://example.com/thumbnail.jpg")
			.startDate(LocalDateTime.now())
			.endDate(LocalDateTime.now().plusDays(10))
			.cost(Cost.FREE)
			.fee(0)
			.member(member)
			.build();
	}

	public static Gallery createFreeGalleryEntity() {
		return Gallery.builder()
			.title("D'ART Gallery")
			.content("This is D'ART Gallery")
			.thumbnail("https://example.com/thumbnail.jpg")
			.startDate(LocalDateTime.now())
			.endDate(null)
			.cost(Cost.FREE)
			.fee(0)
			.member(MemberFixture.createMemberEntity())
			.build();
	}

	public static Gallery createPaidGalleryEntity(LocalDateTime startDate, LocalDateTime endDate) {
		return Gallery.builder()
			.title("D'ART Gallery")
			.content("This is D'ART Gallery")
			.thumbnail("https://example.com/thumbnail.jpg")
			.startDate(startDate)
			.endDate(endDate)
			.cost(Cost.PAY)
			.fee(1000)
			.member(MemberFixture.createMemberEntity())
			.build();
	}

	public static CreateGalleryDto createGalleryEntityForCreateGalleryDto() {
		return CreateGalleryDto.builder()
			.title("D'ART Gallery")
			.content("This is D'ART Gallery")
			.startDate(LocalDateTime.now())
			.endDate(LocalDateTime.now().plusDays(10))
			.fee(1000)
			.hashtags(List.of("happy", "good", "excellent"))
			.informations(List.of(
				new ImageInfoDto("image1.jpg", "Image 1"),
				new ImageInfoDto("image2.jpg", "Image 2"))
			)
			.build();
	}

	public static MultipartFile createMultipartFileForThumbnail() {
		return new MockMultipartFile(
			"thumbnail",
			"thumbnail.png",
			MediaType.IMAGE_PNG_VALUE,
			"Thumbnail Content".getBytes()
		);
	}

	public static List<MultipartFile> createMultipartFileForImages() {
		MockMultipartFile imageFile1 = new MockMultipartFile(
			"imageFiles",
			"image1.png",
			MediaType.IMAGE_PNG_VALUE,
			"Image1 Content".getBytes()
		);

		MockMultipartFile imageFile2 = new MockMultipartFile(
			"imageFiles",
			"image2.png",
			MediaType.IMAGE_PNG_VALUE,
			"Image2 Content".getBytes()
		);

		return List.of(imageFile1, imageFile2);
	}
}
