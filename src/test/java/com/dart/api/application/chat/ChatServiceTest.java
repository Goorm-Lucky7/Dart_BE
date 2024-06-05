package com.dart.api.application.chat;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dart.api.domain.chat.entity.ChatRoom;
import com.dart.api.domain.chat.repo.ChatRoomRepository;
import com.dart.api.domain.gallery.entity.Gallery;
import com.dart.support.GalleryFixture;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

	@Mock
	private ChatRoomRepository chatRoomRepository;

	@InjectMocks
	private ChatService chatService;

	@Test
	@DisplayName("CREATE CHATROOM(⭕️ SUCCESS): 사용자가 성공적으로 채팅방 생성을 완료했습니다.")
	void createChatRoom() {
		// GIVEN
		Gallery gallery = GalleryFixture.createGalleryEntity();

		// WHEN
		chatService.createChatRoom(gallery);

		// THEN
		verify(chatRoomRepository, times(1)).save(any(ChatRoom.class));
	}
}
