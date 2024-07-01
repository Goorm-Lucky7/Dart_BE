package com.dart.support;

import static com.dart.global.common.util.ChatConstant.*;

import java.time.LocalDateTime;

import com.dart.api.domain.chat.entity.ChatMessage;
import com.dart.api.domain.chat.entity.ChatRoom;
import com.dart.api.domain.gallery.entity.Gallery;
import com.dart.api.domain.member.entity.Member;
import com.dart.api.dto.chat.request.ChatMessageCreateDto;
import com.dart.api.dto.chat.request.ChatMessageSendDto;

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

	public static ChatMessage createChatMessageEntity(ChatRoom chatRoom, Member member,
		ChatMessageCreateDto chatMessageCreateDto
	) {
		return ChatMessage.builder()
			.content(chatMessageCreateDto.content())
			.createdAt(chatMessageCreateDto.createdAt())
			.isAuthor(chatMessageCreateDto.isAuthor())
			.chatRoom(chatRoom)
			.member(member)
			.build();
	}

	public static ChatMessageCreateDto createChatMessageEntityForChatMessageCreateDto() {
		return ChatMessageCreateDto.builder()
			.content("Hello 👋🏻")
			.createdAt(LocalDateTime.now())
			.nickname("test1")
			.profileImageUrl("https://example.com/profile.jpg")
			.isAuthor(false)
			.build();
	}

	public static ChatMessageSendDto createChatMessageSendDto(Long memberId, Long chatRoomId, String content,
		LocalDateTime createdAt, boolean isAuthor
	) {
		return ChatMessageSendDto.builder()
			.memberId(memberId)
			.chatRoomId(chatRoomId)
			.content(content)
			.createdAt(createdAt)
			.isAuthor(isAuthor)
			.expirySeconds(CHAT_MESSAGE_EXPIRY_SECONDS)
			.build();
	}
}
