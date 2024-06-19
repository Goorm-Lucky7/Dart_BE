package com.dart.support;

import java.time.LocalDateTime;

import com.dart.api.domain.chat.entity.ChatMessage;
import com.dart.api.domain.chat.entity.ChatRoom;
import com.dart.api.domain.gallery.entity.Gallery;
import com.dart.api.dto.chat.request.ChatMessageCreateDto;
import com.dart.api.dto.chat.response.ChatMessageReadDto;

public class ChatFixture {

	public static ChatRoom createChatRoomEntity() {
		return ChatRoom.createChatRoom(GalleryFixture.createGalleryEntity());
	}

	public static ChatRoom createChatRoomEntity(Gallery gallery) {
		return ChatRoom.builder()
			.title(gallery.getTitle())
			.gallery(gallery)
			.build();
	}

	public static ChatMessage createChatMessageEntity() {
		return ChatMessage.createChatMessage(
			createChatRoomEntity(),
			MemberFixture.createMemberEntity(),
			createChatMessageEntityForChatMessageCreateDto()
		);
	}

	public static ChatMessage createChatMessageEntity(ChatRoom chatRoom) {
		return ChatMessage.createChatMessage(
			chatRoom,
			MemberFixture.createMemberEntity(),
			createChatMessageEntityForChatMessageCreateDto()
		);
	}

	public static ChatMessage createChatMessageEntity(ChatRoom chatRoom, String content) {
		return ChatMessage.createChatMessage(
			chatRoom,
			MemberFixture.createMemberEntity(),
			createChatMessageEntityForChatMessageCreateDto(content)
		);
	}

	public static ChatMessageCreateDto createChatMessageEntityForChatMessageCreateDto() {
		return ChatMessageCreateDto.builder()
			.content("Hello 👋🏻")
			.build();
	}

	public static ChatMessageCreateDto createChatMessageEntityForChatMessageCreateDto(String content) {
		return ChatMessageCreateDto.builder()
			.content(content)
			.build();
	}

	public static ChatMessageReadDto createChatMessageReadDto(String sender, String content, LocalDateTime createdAt,
		boolean isAuthor
	) {
		return ChatMessageReadDto.builder()
			.sender(sender)
			.content(content)
			.createdAt(createdAt)
			.isAuthor(isAuthor)
			.build();
	}

	public static String createMessageValue(String sender, String content, LocalDateTime createdAt, boolean isAuthor) {
		return sender + "|" + content + "|" + createdAt.toString() + "|" + isAuthor;
	}
}
