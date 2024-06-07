package com.dart.api.domain.chat.entity;

import com.dart.api.domain.gallery.entity.Gallery;
import com.dart.global.common.entity.BaseTimeEntity;

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
@Table(name = "tbl_chat_room")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom extends BaseTimeEntity {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "title", nullable = false)
	private String title;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "gallery_id")
	private Gallery gallery;

	@Builder
	private ChatRoom(String title, Gallery gallery) {
		this.title = title;
		this.gallery = gallery;
	}

	public static ChatRoom createChatRoom(Gallery gallery) {
		return ChatRoom.builder()
			.title(gallery.getTitle())
			.gallery(gallery)
			.build();
	}
}

