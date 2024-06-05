package com.dart.api.domain.chat.entity;

import org.hibernate.annotations.ColumnDefault;

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
@Table(name = "tbl_chat_message")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage extends BaseTimeEntity {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "content", nullable = false)
	private String content;

	@Column(name = "is_blinded", nullable = false)
	@ColumnDefault("false")
	private boolean isBlinded;

	@Column(name = "sender", nullable = false)
	private String sender;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "chatroom_id")
	private ChatRoom chatroom;

	@Builder
	private ChatMessage(
		String content,
		boolean isBlinded,
		String sender,
		ChatRoom chatroom
	) {
		this.content = content;
		this.isBlinded = isBlinded;
		this.sender = sender;
		this.chatroom = chatroom;
	}
}
