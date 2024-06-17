package com.dart.api.application.chat;

import static com.dart.global.common.util.ChatConstant.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;

import com.dart.api.domain.auth.entity.AuthUser;
import com.dart.api.domain.chat.entity.ChatMessage;
import com.dart.api.domain.chat.entity.ChatRoom;
import com.dart.api.domain.chat.repository.ChatMessageRepository;
import com.dart.api.domain.chat.repository.ChatRedisRepository;
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

	@Mock
	private SimpMessageHeaderAccessor simpMessageHeaderAccessor;

	@Mock
	private ChatRedisRepository chatRedisRepository;

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
	@DisplayName("DELETE CHATROOM(⭕️ SUCCESS): 사용자가 성공적으로 채팅방과 채팅메시지 삭제를 완료했습니다.")
	void deleteChatRoom_void_success() {
		// GIVEN
		Gallery gallery = GalleryFixture.createGalleryEntity();
		ChatRoom chatRoom = ChatFixture.createChatRoomEntity();
		List<ChatMessage> chatMessages = List.of(
			ChatFixture.createChatMessageEntity(chatRoom),
			ChatFixture.createChatMessageEntity(chatRoom)
		);

		given(chatRoomRepository.findByGallery(gallery)).willReturn(Optional.of(chatRoom));
		given(chatMessageRepository.findByChatRoom(chatRoom)).willReturn(chatMessages);

		// WHEN
		chatService.deleteChatRoom(gallery);

		// THEN
		verify(chatRoomRepository).findByGallery(gallery);
		verify(chatMessageRepository).findByChatRoom(chatRoom);
		verify(chatMessageRepository).deleteAll(chatMessages);
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
			() -> chatService.deleteChatRoom(gallery))
			.isInstanceOf(NotFoundException.class)
			.hasMessage("[❎ ERROR] 요청하신 채팅방을 찾을 수 없습니다.");

		verify(chatRoomRepository, times(1)).findByGallery(gallery);
		verify(chatMessageRepository, times(0)).findByChatRoom(any(ChatRoom.class));
		verify(chatMessageRepository, times(0)).deleteAll(anyList());
		verify(chatRoomRepository, times(0)).delete(any(ChatRoom.class));
	}

	@Test
	@DisplayName("SAVE CHAT MESSAGE(⭕️ SUCCESS): 사용자가 성공적으로 채팅 메시지를 전송 및 저장했습니다.")
	void saveChatMessage_void_success() {
		// GIVEN
		Long chatRoomId = 1L;
		String memberEmail = "test1@example.com";

		Member member = MemberFixture.createMemberEntity();
		AuthUser authUser = MemberFixture.createAuthUserEntity();
		ChatMessageCreateDto chatMessageCreateDto = ChatFixture.createChatMessageEntityForChatMessageCreateDto();
		ChatRoom chatRoom = ChatFixture.createChatRoomEntity();

		Map<String, Object> sessionAttributes = new HashMap<>();
		sessionAttributes.put(CHAT_SESSION_USER, authUser);

		given(chatRoomRepository.findById(chatRoomId)).willReturn(Optional.of(chatRoom));
		given(memberRepository.findByEmail(memberEmail)).willReturn(Optional.of(member));
		given(simpMessageHeaderAccessor.getSessionAttributes()).willReturn(sessionAttributes);

		// WHEN
		chatService.saveChatMessage(chatRoomId, chatMessageCreateDto, simpMessageHeaderAccessor);

		// THEN
		ArgumentCaptor<String> contentCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> senderCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<LocalDateTime> createdAtCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
		ArgumentCaptor<Long> expiryCaptor = ArgumentCaptor.forClass(Long.class);

		verify(chatRedisRepository).saveChatMessage(
			any(ChatRoom.class),
			contentCaptor.capture(),
			senderCaptor.capture(),
			createdAtCaptor.capture(),
			expiryCaptor.capture()
		);

		assertThat(contentCaptor.getValue()).isEqualTo(chatMessageCreateDto.content());
		assertThat(senderCaptor.getValue()).isEqualTo(member.getNickname());
		assertThat(expiryCaptor.getValue()).isGreaterThan(0);
	}

	@Test
	@DisplayName("SAVE CHAT MESSAGE(❌ FAILURE): 존재하지 않은 채팅방으로 채팅 메세지를 전송했습니다.")
	void saveChatMessage_chatRoom_NotFoundException_fail() {
		// GIVEN
		Long chatRoomId = 1L;

		ChatMessageCreateDto chatMessageCreateDto = ChatFixture.createChatMessageEntityForChatMessageCreateDto();

		given(chatRoomRepository.findById(chatRoomId)).willReturn(Optional.empty());

		// WHEN & THEN
		assertThatThrownBy(
			() -> chatService.saveChatMessage(chatRoomId, chatMessageCreateDto, simpMessageHeaderAccessor))
			.isInstanceOf(NotFoundException.class)
			.hasMessage("[❎ ERROR] 요청하신 채팅방을 찾을 수 없습니다.");

		verify(chatRoomRepository, times(1)).findById(chatRoomId);
		verify(memberRepository, times(0)).findByEmail(any(String.class));
		verify(chatMessageRepository, times(0)).save(any(ChatMessage.class));
	}

	@Test
	@DisplayName("SAVE CHAT MESSAGE(❌ FAILURE): 존재하지 않은 사용자 이메일로 채팅 메세지를 전송했습니다.")
	void saveChatMessage_member_UnauthorizedException_fail() {
		// GIVEN
		Long chatRoomId = 1L;
		String memberEmail = "test1@example.com";

		AuthUser authUser = MemberFixture.createAuthUserEntity();
		ChatMessageCreateDto chatMessageCreateDto = ChatFixture.createChatMessageEntityForChatMessageCreateDto();
		ChatRoom chatRoom = ChatFixture.createChatRoomEntity();

		Map<String, Object> sessionAttributes = new HashMap<>();
		sessionAttributes.put(CHAT_SESSION_USER, authUser);

		given(chatRoomRepository.findById(chatRoomId)).willReturn(Optional.of(chatRoom));
		given(memberRepository.findByEmail(memberEmail)).willReturn(Optional.empty());
		given(simpMessageHeaderAccessor.getSessionAttributes()).willReturn(sessionAttributes);

		// WHEN & THEN
		assertThatThrownBy(
			() -> chatService.saveChatMessage(chatRoomId, chatMessageCreateDto, simpMessageHeaderAccessor))
			.isInstanceOf(UnauthorizedException.class)
			.hasMessage("[❎ ERROR] 로그인이 필요한 기능입니다.");

		verify(chatRoomRepository, times(1)).findById(chatRoomId);
		verify(memberRepository, times(1)).findByEmail(memberEmail);
		verify(chatMessageRepository, times(0)).save(any(ChatMessage.class));
	}

	@Test
	@DisplayName("DETERMINE EXPIRY PAID GALLERY(⭕️ SUCCESS): 성공적으로 유료 전시회의 채팅 메시지 만료 시간을 계산했습니다.")
	void determineExpiry_paid_success() {
		// GIVEN
		Gallery gallery = GalleryFixture.createPaidGalleryEntity(5);
		ChatRoom chatRoom = ChatFixture.createChatRoomEntity(gallery);

		// WHEN
		long actualExpiry = chatService.determineExpiry(chatRoom);

		// THEN
		assertThat(actualExpiry).isGreaterThan(0);
	}

	@Test
	@DisplayName("DETERMINE EXPIRY FREE GALLERY(⭕️ SUCCESS): 성공적으로 무료 전시회의 채팅 메시지 만료 시간을 설정했습니다.")
	void determineExpiry_free_success() {
		// GIVEN
		Gallery gallery = GalleryFixture.createFreeGalleryEntity();
		ChatRoom chatRoom = ChatFixture.createChatRoomEntity(gallery);

		// WHEN
		long actualExpiry = chatService.determineExpiry(chatRoom);

		// THEN
		assertThat(actualExpiry).isEqualTo(FREE_MESSAGE_EXPIRY_SECONDS);
	}
}
