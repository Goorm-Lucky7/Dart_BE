package com.dart.api.application.chat;

import static com.dart.global.common.util.ChatConstant.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import com.dart.api.dto.chat.response.ChatMessageReadDto;
import com.dart.api.dto.page.PageInfo;
import com.dart.api.dto.page.PageResponse;
import com.dart.global.error.exception.NotFoundException;
import com.dart.global.error.exception.UnauthorizedException;
import com.dart.support.ChatFixture;
import com.dart.support.GalleryFixture;
import com.dart.support.MemberFixture;

@ExtendWith(MockitoExtension.class)
class ChatMessageServiceTest {

	@Mock
	private ChatRoomRepository chatRoomRepository;

	@Mock
	private MemberRepository memberRepository;

	@Mock
	private SimpMessageHeaderAccessor simpMessageHeaderAccessor;

	@Mock
	private ChatRedisRepository chatRedisRepository;

	@Mock
	private ChatMessageRepository chatMessageRepository;

	@InjectMocks
	private ChatMessageService chatMessageService;

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

		when(simpMessageHeaderAccessor.getSessionAttributes()).thenReturn(Map.of(CHAT_SESSION_USER, authUser));

		when(chatRoomRepository.findById(chatRoomId)).thenReturn(Optional.of(chatRoom));
		when(memberRepository.findByEmail(memberEmail)).thenReturn(Optional.of(member));

		ArgumentCaptor<ChatMessage> chatMessageArgumentCaptor = ArgumentCaptor.forClass(ChatMessage.class);

		// WHEN
		chatMessageService.saveChatMessage(chatRoomId, chatMessageCreateDto, simpMessageHeaderAccessor);

		// THEN
		verify(chatMessageRepository).save(chatMessageArgumentCaptor.capture());
		verify(chatRedisRepository).saveChatMessage(
			eq(chatRoom),
			eq(chatMessageArgumentCaptor.getValue().getContent()),
			eq(chatMessageArgumentCaptor.getValue().getSender()),
			any(LocalDateTime.class),
			eq(CHAT_MESSAGE_EXPIRY_SECONDS)
		);

		assertEquals(chatMessageCreateDto.content(), chatMessageArgumentCaptor.getValue().getContent());
		assertEquals(member.getNickname(), chatMessageArgumentCaptor.getValue().getSender());
		assertEquals(chatRoom, chatMessageArgumentCaptor.getValue().getChatRoom());
	}

	@Test
	@DisplayName("SAVE CHAT MESSAGE(❌ FAILURE): 존재하지 않은 채팅방으로 채팅 메세지를 전송했습니다.")
	void saveChatMessage_chatRoom_NotFoundException_fail() {
		// GIVEN
		Long chatRoomId = 1L;

		ChatMessageCreateDto chatMessageCreateDto = ChatFixture.createChatMessageEntityForChatMessageCreateDto();

		when(chatRoomRepository.findById(chatRoomId)).thenReturn(Optional.empty());

		// WHEN & THEN
		assertThatThrownBy(
			() -> chatMessageService.saveChatMessage(chatRoomId, chatMessageCreateDto, simpMessageHeaderAccessor))
			.isInstanceOf(NotFoundException.class)
			.hasMessage("[❎ ERROR] 요청하신 채팅방을 찾을 수 없습니다.");

		verify(chatRoomRepository, times(1)).findById(chatRoomId);
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
			() -> chatMessageService.saveChatMessage(chatRoomId, chatMessageCreateDto, simpMessageHeaderAccessor))
			.isInstanceOf(UnauthorizedException.class)
			.hasMessage("[❎ ERROR] 로그인이 필요한 기능입니다.");

		verify(chatRoomRepository, times(1)).findById(chatRoomId);
		verify(memberRepository, times(1)).findByEmail(memberEmail);
	}

	@Test
	@DisplayName("GET CHAT MESSAGE LIST(⭕️ SUCCESS): 성공적으로 Redis에서 채팅 메시지 목록을 조회했습니다.")
	void getChatMessageList_Redis_void_success() {
		// GIVEN
		int page = 0;
		int size = 10;

		Member member = MemberFixture.createMemberEntity();
		Member author = MemberFixture.createMemberEntityForAuthor();
		Gallery gallery = GalleryFixture.createGalleryEntityForAuthor();
		ChatRoom chatRoom = ChatFixture.createChatRoomEntity(gallery);
		Long chatRoomId = chatRoom.getId();

		List<ChatMessageReadDto> redisMessages = List.of(
			new ChatMessageReadDto(member.getNickname(), "Hello 👋🏻", LocalDateTime.now(), false),
			new ChatMessageReadDto(author.getNickname(), "Have a good time 👏", LocalDateTime.now(), false)
		);

		PageResponse<ChatMessageReadDto> redisResponse = new PageResponse<>(redisMessages, new PageInfo(page, true));

		// Redis에서 데이터를 가져오는 경우
		when(chatRedisRepository.getChatMessageReadDto(chatRoomId, page, size)).thenReturn(redisResponse);

		// WHEN
		PageResponse<ChatMessageReadDto> actualResponse = chatMessageService.getChatMessageList(chatRoomId, page, size);

		// THEN
		assertEquals(2, actualResponse.pages().size());

		ChatMessageReadDto firstMessage = actualResponse.pages().get(0);
		assertEquals(member.getNickname(), firstMessage.sender());
		assertEquals("Hello 👋🏻", firstMessage.content());
		assertFalse(firstMessage.isAuthor());

		ChatMessageReadDto secondMessage = actualResponse.pages().get(1);
		assertEquals(author.getNickname(), secondMessage.sender());
		assertEquals("Have a good time 👏", secondMessage.content());
		assertFalse(secondMessage.isAuthor());

		// Redis가 호출된 것을 확인
		verify(chatRedisRepository, times(1)).getChatMessageReadDto(chatRoomId, page, size);
		verifyNoInteractions(chatRoomRepository);
		verifyNoInteractions(chatMessageRepository);
	}

	@Test
	@DisplayName("GET CHAT MESSAGE LIST(⭕️ SUCCESS): 성공적으로 MySQL에서 채팅 메시지 목록을 조회했습니다.")
	void getChatMessageList_MySQL_void_success() {
		// GIVEN
		int page = 0;
		int size = 10;

		Member member = MemberFixture.createMemberEntity();
		Member author = MemberFixture.createMemberEntityForAuthor();
		Gallery gallery = GalleryFixture.createGalleryEntityForAuthor();
		ChatRoom chatRoom = ChatFixture.createChatRoomEntity(gallery);
		Long chatRoomId = chatRoom.getId();

		Pageable pageable = PageRequest.of(page, size);

		List<ChatMessage> chatMessageList = List.of(
			ChatMessage.createChatMessage(chatRoom, member, new ChatMessageCreateDto("Hello 👋🏻")),
			ChatMessage.createChatMessage(chatRoom, author, new ChatMessageCreateDto("Have a good time 👏"))
		);

		Page<ChatMessage> mySQLMessages = new PageImpl<>(chatMessageList, pageable, chatMessageList.size());

		// Redis에 데이터가 없는 경우
		when(chatRedisRepository.getChatMessageReadDto(chatRoomId, page, size))
			.thenReturn(new PageResponse<>(new ArrayList<>(), new PageInfo(page, false)));
		when(chatRoomRepository.findById(chatRoomId))
			.thenReturn(Optional.of(chatRoom));
		when(chatMessageRepository.findByChatRoom(any(ChatRoom.class), any(Pageable.class)))
			.thenReturn(mySQLMessages);

		// WHEN
		PageResponse<ChatMessageReadDto> actualResponse = chatMessageService.getChatMessageList(chatRoomId, page, size);

		// THEN
		assertEquals(2, actualResponse.pages().size());

		ChatMessageReadDto firstMessage = actualResponse.pages().get(0);
		assertEquals(member.getNickname(), firstMessage.sender());
		assertEquals("Hello 👋🏻", firstMessage.content());
		assertFalse(firstMessage.isAuthor());

		ChatMessageReadDto secondMessage = actualResponse.pages().get(1);
		assertEquals(author.getNickname(), secondMessage.sender());
		assertEquals("Have a good time 👏", secondMessage.content());
		assertFalse(secondMessage.isAuthor());

		// DB와 Redis가 호출된 것을 확인
		verify(chatRedisRepository, times(1)).getChatMessageReadDto(chatRoomId, page, size);
		verify(chatRoomRepository, times(1)).findById(chatRoomId);
		verify(chatMessageRepository, times(1)).findByChatRoom(any(ChatRoom.class), any(Pageable.class));
	}
}
