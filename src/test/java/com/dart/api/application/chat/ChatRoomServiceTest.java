package com.dart.api.application.chat;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dart.api.domain.chat.entity.ChatRoom;
import com.dart.api.domain.chat.repository.ChatRedisRepository;
import com.dart.api.domain.chat.repository.ChatRoomRepository;
import com.dart.api.domain.gallery.entity.Gallery;
import com.dart.global.error.exception.NotFoundException;
import com.dart.support.ChatFixture;
import com.dart.support.GalleryFixture;

@ExtendWith(MockitoExtension.class)
class ChatRoomServiceTest {

	@Mock
	private ChatRoomRepository chatRoomRepository;

	@Mock
	private ChatRedisRepository chatRedisRepository;

	@InjectMocks
	private ChatRoomService chatRoomService;

	@Test
	@DisplayName("CREATE CHATROOM(⭕️ SUCCESS): 사용자가 성공적으로 채팅방 생성을 완료했습니다.")
	void createChatRoom_void_success() {
		// GIVEN
		Gallery gallery = GalleryFixture.createGalleryEntity();

		// WHEN
		chatRoomService.createChatRoom(gallery);

		// THEN
		verify(chatRoomRepository, times(1)).save(any(ChatRoom.class));
	}

	@Test
	@DisplayName("DELETE CHATROOM(⭕️ SUCCESS): 사용자가 성공적으로 채팅방과 채팅메시지 삭제를 완료했습니다.")
	void deleteChatRoom_void_success() {
		// GIVEN
		Gallery gallery = GalleryFixture.createGalleryEntity();
		ChatRoom chatRoom = ChatFixture.createChatRoomEntity();

		given(chatRoomRepository.findByGallery(gallery)).willReturn(Optional.of(chatRoom));

		// WHEN
		chatRoomService.deleteChatRoom(gallery);

		// THEN
		verify(chatRoomRepository).findByGallery(gallery);
		verify(chatRedisRepository).deleteChatMessages(chatRoom.getId());
		verify(chatRoomRepository).delete(chatRoom);
	}

	@Test
	@DisplayName("DELETE CHATROOM(❌ FAILURE): 존재하지 않는 채팅방을 삭제하려고 시도했습니다.")
	void deleteChatRoom_NotFoundException_fail() {
		// GIVEN
		Gallery gallery = GalleryFixture.createGalleryEntity();

		given(chatRoomRepository.findByGallery(gallery)).willReturn(Optional.empty());

		// WHEN & THEN
		assertThatThrownBy(
			() -> chatRoomService.deleteChatRoom(gallery))
			.isInstanceOf(NotFoundException.class)
			.hasMessage("[❎ ERROR] 요청하신 채팅방을 찾을 수 없습니다.");

		verify(chatRoomRepository, times(1)).findByGallery(gallery);
		verify(chatRedisRepository, times(0)).deleteChatMessages(anyLong());
		verify(chatRoomRepository, times(0)).delete(any(ChatRoom.class));
	}
}
