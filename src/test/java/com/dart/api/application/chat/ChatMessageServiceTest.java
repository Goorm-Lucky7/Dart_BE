package com.dart.api.application.chat;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.dart.api.domain.chat.entity.ChatMessage;
import com.dart.api.domain.chat.entity.ChatRoom;
import com.dart.api.domain.chat.repository.ChatMessageRepository;
import com.dart.api.domain.chat.repository.ChatRedisRepository;
import com.dart.api.domain.chat.repository.ChatRoomRepository;
import com.dart.api.domain.member.entity.Member;
import com.dart.api.domain.member.repository.MemberRepository;
import com.dart.api.dto.chat.request.ChatMessageCreateDto;
import com.dart.api.dto.chat.request.ChatMessageSendDto;
import com.dart.api.dto.chat.response.ChatMessageReadDto;
import com.dart.api.dto.page.PageInfo;
import com.dart.api.dto.page.PageResponse;
import com.dart.global.error.exception.NotFoundException;
import com.dart.support.ChatFixture;
import com.dart.support.MemberFixture;

@ExtendWith(MockitoExtension.class)
class ChatMessageServiceTest {

	@Mock
	private ChatRoomRepository chatRoomRepository;

	@Mock
	private MemberRepository memberRepository;

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
		ChatMessageCreateDto chatMessageCreateDto = ChatFixture.createChatMessageEntityForChatMessageCreateDto();

		ChatRoom chatRoom = ChatFixture.createChatRoomEntity();
		Member member = MemberFixture.createMemberEntity();
		ChatMessage chatMessage = ChatFixture.createChatMessageEntity(chatRoom, member, chatMessageCreateDto);

		when(chatRoomRepository.findById(anyLong())).thenReturn(Optional.of(chatRoom));
		when(memberRepository.findByNickname(anyString())).thenReturn(Optional.of(member));
		when(chatMessageRepository.save(any(ChatMessage.class))).thenReturn(chatMessage);

		// WHEN
		chatMessageService.saveChatMessage(chatRoomId, chatMessageCreateDto);

		// THEN
		verify(chatMessageRepository, times(1)).save(any(ChatMessage.class));
		verify(chatRedisRepository, times(1)).saveChatMessage(any(ChatMessageSendDto.class), any(Member.class));
	}

	@Test
	@DisplayName("SAVE CHAT MESSAGE(❌ FAILURE): 존재하지 않은 채팅방으로 채팅 메세지를 전송했습니다.")
	void saveChatMessage_chatRoom_NotFoundException_fail() {
		// GIVEN
		Long chatRoomId = 1L;
		ChatMessageCreateDto chatMessageCreateDto = ChatFixture.createChatMessageEntityForChatMessageCreateDto();

		when(chatRoomRepository.findById(anyLong())).thenReturn(Optional.empty());

		// WHEN & THEN
		assertThatThrownBy(
			() -> chatMessageService.saveChatMessage(chatRoomId, chatMessageCreateDto))
			.isInstanceOf(NotFoundException.class)
			.hasMessage("[❎ ERROR] 요청하신 채팅방을 찾을 수 없습니다.");
	}

	@Test
	@DisplayName("SAVE CHAT MESSAGE(❌ FAILURE): 존재하지 않은 사용자 닉네임으로 채팅 메세지를 전송했습니다.")
	void saveChatMessage_member_NotFoundException_fail() {
		// GIVEN
		Long chatRoomId = 1L;
		ChatMessageCreateDto chatMessageCreateDto = ChatFixture.createChatMessageEntityForChatMessageCreateDto();

		ChatRoom chatRoom = ChatFixture.createChatRoomEntity();

		when(chatRoomRepository.findById(anyLong())).thenReturn(Optional.of(chatRoom));
		when(memberRepository.findByNickname(anyString())).thenReturn(Optional.empty());

		// WHEN & THEN
		assertThatThrownBy(
			() -> chatMessageService.saveChatMessage(chatRoomId, chatMessageCreateDto))
			.isInstanceOf(NotFoundException.class)
			.hasMessage("[❎ ERROR] 요청하신 회원을 찾을 수 없습니다.");
	}

	@Test
	@DisplayName("GET CHAT MESSAGE LIST(⭕️ SUCCESS): 성공적으로 REDIS에 존재하는 채팅 메시지 목록을 조회했습니다.")
	void getChatMessageList_Redis_void_success() {
		// GIVEN
		Long chatRoomId = 1L;
		int page = 0;
		int size = 10;

		List<ChatMessageReadDto> chatMessageReadDtoList = List.of(
			new ChatMessageReadDto("sender1", "content1", LocalDateTime.now(), true, "profileImageURL1"),
			new ChatMessageReadDto("sender2", "content2", LocalDateTime.now(), true, "profileImageURL2")
		);

		PageResponse<ChatMessageReadDto> pageResponse = new PageResponse<>(
			chatMessageReadDtoList, new PageInfo(page, true)
		);

		when(chatRedisRepository.getChatMessageReadDto(anyLong(), anyInt(), anyInt())).thenReturn(pageResponse);

		// WHEN
		PageResponse<ChatMessageReadDto> actualPageResponse = chatMessageService.getChatMessageList(
			chatRoomId, page, size
		);

		// THEN
		assertEquals(2, actualPageResponse.pages().size());
		assertEquals("sender1", actualPageResponse.pages().get(0).sender());
		assertEquals("sender2", actualPageResponse.pages().get(1).sender());
		assertTrue(actualPageResponse.pageInfo().isDone());
	}

	@Test
	@DisplayName("GET CHAT MESSAGE LIST(⭕️ SUCCESS): 성공적으로 MySQL에 존재하는 채팅 메시지 목록을 조회했습니다.")
	void getChatMessageList_MySQL_void_success() {
		// GIVEN
		Long chatRoomId = 1L;
		int page = 0;
		int size = 10;

		ChatRoom chatRoom = ChatFixture.createChatRoomEntity();
		Member member = MemberFixture.createMemberEntity();
		ChatMessageCreateDto chatMessageCreateDto = ChatFixture.createChatMessageEntityForChatMessageCreateDto();

		List<ChatMessage> chatMessages = List.of(
			ChatMessage.chatMessageFromCreateDto(chatRoom, member, chatMessageCreateDto)
		);

		Page<ChatMessage> chatMessagePage = new PageImpl<>(
			chatMessages, PageRequest.of(page, size), chatMessages.size()
		);

		when(chatRedisRepository.getChatMessageReadDto(anyLong(), anyInt(), anyInt()))
			.thenReturn(null);
		when(chatRoomRepository.findById(anyLong()))
			.thenReturn(Optional.of(chatRoom));
		when(chatMessageRepository.findByChatRoomOrderByCreatedAtDesc(any(ChatRoom.class), any(Pageable.class)))
			.thenReturn(chatMessagePage);
		when(memberRepository.findByNickname(anyString()))
			.thenReturn(Optional.of(member));

		// WHEN
		PageResponse<ChatMessageReadDto> actualPageResponse = chatMessageService.getChatMessageList(
			chatRoomId, page, size
		);

		// THEN
		assertEquals(1, actualPageResponse.pages().size());
		verify(chatRedisRepository, times(1)).saveChatMessage(any(ChatMessageSendDto.class), any(Member.class));
	}
}
