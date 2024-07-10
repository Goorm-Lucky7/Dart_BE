package com.dart.api.application.chat;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dart.api.domain.chat.entity.ChatMessage;
import com.dart.api.domain.chat.entity.ChatRoom;
import com.dart.api.domain.chat.repository.ChatMessageRepository;
import com.dart.api.domain.chat.repository.ChatRedisRepository;
import com.dart.api.domain.chat.repository.ChatRoomRepository;
import com.dart.api.domain.member.entity.Member;
import com.dart.api.domain.member.repository.MemberRepository;
import com.dart.api.dto.chat.request.ChatMessageCreateDto;
import com.dart.api.dto.chat.request.ChatMessageSendDto;
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

		when(chatRoomRepository.findById(anyLong())).thenReturn(Optional.of(chatRoom));
		when(memberRepository.findByNickname(anyString())).thenReturn(Optional.of(member));

		// WHEN
		chatMessageService.saveChatMessage(chatRoomId, chatMessageCreateDto);

		// THEN
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
	@DisplayName("BATCH SAVE MESSAGES(⭕️ SUCCESS): 성공적으로 활성화된 채팅방이 2개일 때 배치 채팅 메시지를 저장했습니다.")
	void batchSaveMessages_twoActiveChatRooms_success() {
		// GIVEN
		Long chatRoomId1 = 1L;
		Long chatRoomId2 = 2L;

		ChatRoom chatRoom1 = ChatFixture.createChatRoomEntity();
		ChatRoom chatRoom2 = ChatFixture.createChatRoomEntity();
		Member member = MemberFixture.createMemberEntity();
		ChatMessageCreateDto chatMessageCreateDto = ChatFixture.createChatMessageEntityForChatMessageCreateDto();
		ChatMessage chatMessage1 = ChatFixture.createChatMessageEntity(chatRoom1, member, chatMessageCreateDto);
		ChatMessage chatMessage2 = ChatFixture.createChatMessageEntity(chatRoom2, member, chatMessageCreateDto);

		List<Long> activeChatRoomIds = List.of(chatRoomId1, chatRoomId2);
		List<ChatMessage> batchMessages1 = List.of(chatMessage1);
		List<ChatMessage> batchMessages2 = List.of(chatMessage2);

		when(chatRedisRepository.getActiveChatRoomIds()).thenReturn(activeChatRoomIds);
		when(chatRedisRepository.getAllBatchMessages(chatRoomId1)).thenReturn(batchMessages1);
		when(chatRedisRepository.getAllBatchMessages(chatRoomId2)).thenReturn(batchMessages2);

		// WHEN
		chatMessageService.batchSaveMessages();

		// THEN
		verify(chatMessageRepository, times(1)).saveAll(batchMessages1);
		verify(chatMessageRepository, times(1)).saveAll(batchMessages2);
		verify(chatRedisRepository, times(1)).deleteChatMessages(chatRoomId1);
		verify(chatRedisRepository, times(1)).deleteChatMessages(chatRoomId2);
	}

	@Test
	@DisplayName("BATCH SAVE MESSAGES(⭕️ SUCCESS): 성공적으로 활성화된 채팅방이 1개일 때 배치 채팅 메시지를 저장했습니다.")
	void batchSaveMessages_oneActiveChatRoom_success() {
		// GIVEN
		Long chatRoomId1 = 1L;
		Long chatRoomId2 = 2L;

		ChatRoom chatRoom1 = ChatFixture.createChatRoomEntity();
		Member member = MemberFixture.createMemberEntity();
		ChatMessageCreateDto chatMessageCreateDto = ChatFixture.createChatMessageEntityForChatMessageCreateDto();
		ChatMessage chatMessage1 = ChatFixture.createChatMessageEntity(chatRoom1, member, chatMessageCreateDto);

		List<Long> activeChatRoomIds = List.of(chatRoomId1, chatRoomId2);
		List<ChatMessage> batchMessages1 = List.of(chatMessage1);
		List<ChatMessage> batchMessages2 = List.of();

		when(chatRedisRepository.getActiveChatRoomIds()).thenReturn(activeChatRoomIds);
		when(chatRedisRepository.getAllBatchMessages(chatRoomId1)).thenReturn(batchMessages1);
		when(chatRedisRepository.getAllBatchMessages(chatRoomId2)).thenReturn(batchMessages2);

		// WHEN
		chatMessageService.batchSaveMessages();

		// THEN
		verify(chatMessageRepository, times(1)).saveAll(batchMessages1);
		verify(chatMessageRepository, never()).saveAll(batchMessages2);
		verify(chatRedisRepository, times(1)).deleteChatMessages(chatRoomId1);
		verify(chatRedisRepository, never()).deleteChatMessages(chatRoomId2);
	}

	@Test
	@DisplayName("BATCH SAVE MESSAGES(⭕️ SUCCESS): 성공적으로 활성화된 채팅방이 없을 때 배치 채팅 메시지를 저장했습니다.")
	void batchSaveMessages_noActiveChatRooms_success() {
		// GIVEN
		Long chatRoomId1 = 1L;
		Long chatRoomId2 = 2L;

		List<Long> activeChatRoomIds = List.of(chatRoomId1, chatRoomId2);
		List<ChatMessage> batchMessages1 = List.of();
		List<ChatMessage> batchMessages2 = List.of();

		when(chatRedisRepository.getActiveChatRoomIds()).thenReturn(activeChatRoomIds);
		when(chatRedisRepository.getAllBatchMessages(chatRoomId1)).thenReturn(batchMessages1);
		when(chatRedisRepository.getAllBatchMessages(chatRoomId2)).thenReturn(batchMessages2);

		// WHEN
		chatMessageService.batchSaveMessages();

		// THEN
		verify(chatMessageRepository, never()).saveAll(anyList());
		verify(chatRedisRepository, never()).deleteChatMessages(anyLong());
	}
}
