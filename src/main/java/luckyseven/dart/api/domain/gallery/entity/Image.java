package luckyseven.dart.api.domain.gallery.entity;

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

	@Column(name = "image_title")
	private String imageTitle;

	@Column(name = "description")
	private String description;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "gallery_id")
	private Gallery gallery;

	@Builder
	private Image(
		String imageUri,
		String imageTitle,
		String description,
		Gallery gallery
	) {
		this.imageUri = imageUri;
		this.imageTitle = imageTitle;
		this.description = description;
		this.gallery = gallery;
	}
}
