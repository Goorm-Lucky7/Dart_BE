package luckyseven.dart.api.domain.chat.entity;

import org.hibernate.annotations.ColumnDefault;

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
import luckyseven.dart.api.domain.gallery.entity.Gallery;
import luckyseven.dart.global.common.BaseTimeEntity;

@Entity
@Getter
@Table(name = "tbl_chatroom")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Chatroom extends BaseTimeEntity {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "title", nullable = false)
	private String title;

	@Column(name = "is_author", nullable = false)
	@ColumnDefault("false")
	private boolean isAuthor;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "gallery_id")
	private Gallery gallery;

	@Builder
	private Chatroom(
		String title,
		boolean isAuthor
	) {
		this.title = title;
		this.isAuthor = isAuthor;
	}
}
