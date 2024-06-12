package com.dart.support;

import com.dart.api.domain.chat.entity.ChatMessage;
import com.dart.api.domain.chat.entity.ChatRoom;
import com.dart.api.dto.chat.request.ChatMessageCreateDto;

public class ChatFixture {

	public static ChatRoom createChatRoomEntity() {
		return ChatRoom.createChatRoom(GalleryFixture.createGalleryEntity());
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

	public static ChatMessageCreateDto createChatMessageEntityForChatMessageCreateDto() {
		return ChatMessageCreateDto.builder()
			.content("Hello 👋🏻")
			.build();
	}
}
