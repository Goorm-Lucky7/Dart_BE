package luckyseven.ddua.api.domain.gallery.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import luckyseven.ddua.api.global.common.BaseTimeEntity;

@Entity
@Getter
@Table(name = "tbl_image")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Image {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "image_uri", nullable = false)
	private String imageUri;

	@Column(name = "thumbnail_uri")
	private String thumbnailUri;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "gallery_id")
	private Gallery gallery;

	@Builder
	private Image(
		String imageUri,
		String thumbnailUri
	) {
		this.imageUri = imageUri;
		this.thumbnailUri = thumbnailUri;
	}
}
