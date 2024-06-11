package com.dart.api.domain.chat.entity;

import com.dart.api.domain.member.entity.Member;
import com.dart.api.dto.chat.request.ChatMessageCreateDto;
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

	@Column(name = "sender", nullable = false)
	private String sender;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "chat_room_id")
	private ChatRoom chatRoom;

	@Builder
	private ChatMessage(String content, String sender, ChatRoom chatRoom) {
		this.content = content;
		this.sender = sender;
		this.chatRoom = chatRoom;
	}

	public static ChatMessage createChatMessage(ChatRoom chatRoom, Member member,
		ChatMessageCreateDto chatMessageCreateDto
	) {
		return ChatMessage.builder()
			.chatRoom(chatRoom)
			.sender(member.getNickname())
			.content(chatMessageCreateDto.content())
			.build();
	}
}
