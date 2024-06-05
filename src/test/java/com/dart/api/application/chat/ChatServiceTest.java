package com.dart.api.application.chat;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dart.api.domain.auth.AuthUser;
import com.dart.api.domain.chat.entity.ChatMessage;
import com.dart.api.domain.chat.entity.ChatRoom;
import com.dart.api.domain.chat.repository.ChatMessageRepository;
import com.dart.api.domain.chat.repository.ChatRoomRepository;
import com.dart.api.domain.gallery.entity.Gallery;
import com.dart.api.domain.member.entity.Member;
import com.dart.api.domain.member.repository.MemberRepository;
import com.dart.api.dto.chat.request.ChatMessageCreateDto;
import com.dart.global.error.exception.NotFoundException;
import com.dart.global.error.exception.UnauthorizedException;
import com.dart.support.ChatFixture;
import com.dart.support.GalleryFixture;
import com.dart.support.MemberFixture;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

	@Mock
	private ChatRoomRepository chatRoomRepository;

	@Mock
	private ChatMessageRepository chatMessageRepository;

	@Mock
	private MemberRepository memberRepository;

	@InjectMocks
	private ChatService chatService;

	@Test
	@DisplayName("CREATE CHATROOM(⭕️ SUCCESS): 사용자가 성공적으로 채팅방 생성을 완료했습니다.")
	void createChatRoom_void_success() {
		// GIVEN
		Gallery gallery = GalleryFixture.createGalleryEntity();

		// WHEN
		chatService.createChatRoom(gallery);

		// THEN
		verify(chatRoomRepository, times(1)).save(any(ChatRoom.class));
	}

	@Test
	@DisplayName("SAVE AND SAVE CHAT MESSAGE(⭕️ SUCCESS): 사용자가 성공적으로 채팅 메시지를 전송 및 저장했습니다.")
	void saveAndSendChatMessage_void_success() {
		// GIVEN
		Long chatRoomId = 1L;
		String memberEmail = "test1@example.com";

		Member member = MemberFixture.createMemberEntity();
		AuthUser authUser = MemberFixture.createAuthUserEntity();
		ChatMessageCreateDto chatMessageCreateDto = ChatFixture.createChatMessageEntityForChatMessageCreateDto();
		ChatRoom chatRoom = ChatFixture.createChatRoomEntity();

		given(chatRoomRepository.findById(chatRoomId)).willReturn(Optional.of(chatRoom));
		given(memberRepository.findByEmail(memberEmail)).willReturn(Optional.of(member));

		// WHEN
		chatService.saveAndSendChatMessage(chatRoomId, authUser, chatMessageCreateDto);

		// THEN
		verify(chatRoomRepository).findById(chatRoomId);
		verify(memberRepository).findByEmail(memberEmail);
		verify(chatMessageRepository).save(any(ChatMessage.class));
	}

	@Test
	@DisplayName("SAVE AND SEND CHAT MESSAGE(❌ FAILURE): 존재하지 않은 채팅방으로 채팅 메세지를 전송했습니다.")
	void saveAndSendChatMessage_chatRoom_NotFoundException_fail() {
		// GIVEN
		Long chatRoomId = 1L;

		AuthUser authUser = MemberFixture.createAuthUserEntity();
		ChatMessageCreateDto chatMessageCreateDto = ChatFixture.createChatMessageEntityForChatMessageCreateDto();

		given(chatRoomRepository.findById(chatRoomId)).willReturn(Optional.empty());

		// WHEN & THEN
		assertThatThrownBy(() -> chatService.saveAndSendChatMessage(chatRoomId, authUser, chatMessageCreateDto))
			.isInstanceOf(NotFoundException.class)
			.hasMessage("[❎ ERROR] 요청하신 채팅방을 찾을 수 없습니다.");

		verify(chatRoomRepository, times(1)).findById(chatRoomId);
		verify(memberRepository, times(0)).findByEmail(any(String.class));
		verify(chatMessageRepository, times(0)).save(any(ChatMessage.class));
	}

	@Test
	@DisplayName("SAVE AND SEND CHAT MESSAGE(❌ FAILURE): 존재하지 않은 사용자 이메일로 채팅 메세지를 전송했습니다.")
	void saveAndSendChatMessage_member_UnauthorizedException_fail() {
		// GIVEN
		Long chatRoomId = 1L;
		String memberEmail = "test1@example.com";

		AuthUser authUser = MemberFixture.createAuthUserEntity();
		ChatMessageCreateDto chatMessageCreateDto = ChatFixture.createChatMessageEntityForChatMessageCreateDto();
		ChatRoom chatRoom = ChatFixture.createChatRoomEntity();

		given(chatRoomRepository.findById(chatRoomId)).willReturn(Optional.of(chatRoom));
		given(memberRepository.findByEmail(memberEmail)).willReturn(Optional.empty());

		// WHEN & THEN
		assertThatThrownBy(() -> chatService.saveAndSendChatMessage(chatRoomId, authUser, chatMessageCreateDto))
			.isInstanceOf(UnauthorizedException.class)
			.hasMessage("[❎ ERROR] 로그인이 필요한 기능입니다.");

		verify(chatRoomRepository, times(1)).findById(chatRoomId);
		verify(memberRepository, times(1)).findByEmail(memberEmail);
		verify(chatMessageRepository, times(0)).save(any(ChatMessage.class));
	}
}
