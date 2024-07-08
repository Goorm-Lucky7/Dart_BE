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
	@DisplayName("BATCH SAVE MESSAGES(⭕️ SUCCESS): REDIS에 저장된 배치 메시지를 데이터베이스에 저장했습니다.")
	void batchSaveMessages_void_success() {
		// GIVEN
		ChatRoom chatRoom = ChatFixture.createChatRoomEntity();
		Member member = MemberFixture.createMemberEntity();
		ChatMessageCreateDto chatMessageCreateDto = ChatFixture.createChatMessageEntityForChatMessageCreateDto();
		ChatMessage chatMessage = ChatFixture.createChatMessageEntity(chatRoom, member, chatMessageCreateDto);

		List<ChatMessage> chatMessageList = List.of(chatMessage);

		when(chatRedisRepository.getAllBatchMessages()).thenReturn(chatMessageList);

		// WHEN
		chatMessageService.batchSaveMessages();

		// THEN
		verify(chatMessageRepository, times(1)).saveAll(chatMessageList);
		verify(chatRedisRepository, times(1)).clearBatchMessages();
	}

	@Test
	@DisplayName("BATCH SAVE MESSAGES(⭕️ SUCCESS): REDIS에 저장된 메시지가 없어 데이터베이스에 저장하지 않습니다.")
	void batchSaveMessages_noMessages_void_success() {
		// GIVEN
		List<ChatMessage> chatMessageList = List.of();

		when(chatRedisRepository.getAllBatchMessages()).thenReturn(chatMessageList);

		// WHEN
		chatMessageService.batchSaveMessages();

		// THEN
		verify(chatMessageRepository, times(0)).saveAll(anyList());
		verify(chatRedisRepository, times(0)).clearBatchMessages();
	}
}
