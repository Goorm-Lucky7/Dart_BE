package com.dart.api.application.chat;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.dart.api.domain.chat.entity.ChatMessage;
import com.dart.api.domain.chat.entity.ChatRoom;
import com.dart.api.domain.chat.repository.ChatMessageRepository;
import com.dart.api.domain.chat.repository.ChatRedisRepository;
import com.dart.api.domain.chat.repository.ChatRoomRepository;
import com.dart.api.domain.member.entity.Member;
import com.dart.api.domain.member.repository.MemberRepository;
import com.dart.api.dto.chat.request.ChatMessageCreateDto;
import com.dart.api.dto.chat.response.ChatMessageReadDto;
import com.dart.api.dto.page.PageInfo;
import com.dart.api.dto.page.PageResponse;
import com.dart.global.error.exception.NotFoundException;
import com.dart.support.ChatFixture;
import com.dart.support.MemberFixture;

@ExtendWith(MockitoExtension.class)
class ChatMessageReadServiceTest {

	@Mock
	private ChatRoomRepository chatRoomRepository;

	@Mock
	private MemberRepository memberRepository;

	@Mock
	private ChatRedisRepository chatRedisRepository;

	@Mock
	private ChatMessageRepository chatMessageRepository;

	@InjectMocks
	private ChatMessageReadService chatMessageReadService;

	@Test
	@DisplayName("GET CHAT MESSAGE LIST(⭕️ SUCCESS): 성공적으로 채팅 메시지 목록을 조회했습니다.")
	void getChatMessageList_void_success() {
		// GIVEN
		Long chatRoomId = 1L;
		int page = 0;
		int size = 10;

		List<ChatMessageReadDto> chatMessages = Arrays.asList(
			new ChatMessageReadDto("member1", "Hello 👋🏻", LocalDateTime.now(), true,
				"https://example.com/profile1.jpg"),
			new ChatMessageReadDto("member2", "Bye 👋🏻", LocalDateTime.now(), true, "https://example.com/profile2.jpg")
		);
		PageResponse<ChatMessageReadDto> pageResponse = new PageResponse<>(chatMessages, new PageInfo(page, true));

		when(chatRedisRepository.getChatMessageReadDto(chatRoomId, page, size)).thenReturn(pageResponse);

		// WHEN
		PageResponse<ChatMessageReadDto> actualPageResponse = chatMessageReadService.getChatMessageList(
			chatRoomId, page, size
		);

		// THEN
		assertThat(actualPageResponse).isEqualTo(pageResponse);
		verify(chatRedisRepository, times(1)).getChatMessageReadDto(chatRoomId, page, size);
	}

	@Test
	@DisplayName("GET CHAT MESSAGE LIST FROM DB(⭕️ SUCCESS): Redis에 없는 경우 DB에서 채팅 메시지 목록을 조회했습니다.")
	void getChatMessageList_fromDb_success() {
		// GIVEN
		Long chatRoomId = 1L;
		int page = 0;
		int size = 10;

		ChatRoom chatRoom = ChatFixture.createChatRoomEntity();
		Member member1 = MemberFixture.createMemberEntityWithEmailAndNickname("member1@example.com", "member1");
		Member member2 = MemberFixture.createMemberEntityWithEmailAndNickname("member2@example.com", "member2");

		ChatMessageCreateDto chatMessageCreateDto1 = ChatFixture.createChatMessageEntityForChatMessageCreateDto();
		ChatMessageCreateDto chatMessageCreateDto2 = ChatFixture.createChatMessageEntityForChatMessageCreateDto();

		List<ChatMessage> chatMessageList = Arrays.asList(
			ChatFixture.createChatMessageEntity(chatRoom, member1, chatMessageCreateDto1),
			ChatFixture.createChatMessageEntity(chatRoom, member2, chatMessageCreateDto2)
		);
		Page<ChatMessage> chatMessagePage = new PageImpl<>(chatMessageList);

		List<ChatMessageReadDto> chatMessages = chatMessageList.stream()
			.map(ChatMessage::toChatMessageReadDto)
			.collect(Collectors.toList());
		PageResponse<ChatMessageReadDto> expectedPageResponse = new PageResponse<>(chatMessages,
			new PageInfo(page, true));

		when(chatRedisRepository.getChatMessageReadDto(chatRoomId, page, size)).thenReturn(null);
		when(chatRoomRepository.findById(chatRoomId)).thenReturn(Optional.of(chatRoom));
		when(chatMessageRepository.findByChatRoomOrderByCreatedAtDesc(any(ChatRoom.class), any(Pageable.class)))
			.thenReturn(chatMessagePage);
		when(memberRepository.findByEmailIn(anyList())).thenReturn(Arrays.asList(member1, member2));
		when(memberRepository.findByNickname(member1.getNickname())).thenReturn(Optional.of(member1));
		when(memberRepository.findByNickname(member2.getNickname())).thenReturn(Optional.of(member2));

		// WHEN
		PageResponse<ChatMessageReadDto> actualPageResponse = chatMessageReadService.getChatMessageList(
			chatRoomId, page, size
		);

		// THEN
		assertThat(actualPageResponse).isEqualTo(expectedPageResponse);
		verify(chatRedisRepository, times(1)).getChatMessageReadDto(chatRoomId, page, size);
		verify(chatRoomRepository, times(2)).findById(chatRoomId);
		verify(chatMessageRepository, times(1)).findByChatRoomOrderByCreatedAtDesc(any(ChatRoom.class),
			any(Pageable.class));
		verify(memberRepository, times(1)).findByEmailIn(anyList());
	}

	@Test
	@DisplayName("GET CHAT MESSAGE LIST(❌ FAILURE): 존재하지 않은 채팅방의 채팅 메시지 목록을 조회했습니다.")
	void getChatMessageList_NotFoundException_fail() {
		// GIVEN
		Long chatRoomId = 1L;
		int page = 0;
		int size = 10;

		when(chatRedisRepository.getChatMessageReadDto(chatRoomId, page, size)).thenReturn(null);
		when(chatRoomRepository.findById(chatRoomId)).thenReturn(Optional.empty());

		// WHEN & THEN
		assertThatThrownBy(() -> chatMessageReadService.getChatMessageList(chatRoomId, page, size))
			.isInstanceOf(NotFoundException.class)
			.hasMessage("[❎ ERROR] 요청하신 채팅방을 찾을 수 없습니다.");
	}

	@Test
	@DisplayName("FETCH CHAT MESSAGES FROM DB AND UPDATE MEMBERS(⭕️ SUCCESS): DB에서 채팅 메시지를 조회하고 회원 정보를 업데이트했습니다.")
	void fetchChatMessagesFromDBAndUpdateMembers_void_success() {
		// GIVEN
		Long chatRoomId = 1L;
		int page = 0;
		int size = 10;

		ChatRoom chatRoom = ChatFixture.createChatRoomEntity();
		Member member1 = MemberFixture.createMemberEntityWithEmailAndNickname("member1@example.com", "member1");
		Member member2 = MemberFixture.createMemberEntityWithEmailAndNickname("member2@example.com", "member2");

		ChatMessageCreateDto chatMessageCreateDto1 = ChatFixture.createChatMessageEntityForChatMessageCreateDto();
		ChatMessageCreateDto chatMessageCreateDto2 = ChatFixture.createChatMessageEntityForChatMessageCreateDto();

		List<ChatMessage> chatMessageList = Arrays.asList(
			ChatFixture.createChatMessageEntity(chatRoom, member1, chatMessageCreateDto1),
			ChatFixture.createChatMessageEntity(chatRoom, member2, chatMessageCreateDto2)
		);
		Page<ChatMessage> chatMessagePage = new PageImpl<>(chatMessageList);

		when(chatRoomRepository.findById(chatRoomId)).thenReturn(Optional.of(chatRoom));
		when(chatMessageRepository.findByChatRoomOrderByCreatedAtDesc(any(ChatRoom.class), any(Pageable.class)))
			.thenReturn(chatMessagePage);
		when(memberRepository.findByEmailIn(anyList())).thenReturn(Arrays.asList(member1, member2));

		// WHEN
		List<ChatMessageReadDto> actualChatMessageReadDtoList =
			chatMessageReadService.fetchChatMessagesFromDBAndUpdateMembers(chatRoomId, page, size);

		// THEN
		assertThat(actualChatMessageReadDtoList).hasSize(2);
		assertThat(actualChatMessageReadDtoList.get(0).sender()).isEqualTo(member1.getNickname());
		assertThat(actualChatMessageReadDtoList.get(0).profileImageUrl()).isEqualTo(member1.getProfileImageUrl());
		assertThat(actualChatMessageReadDtoList.get(1).sender()).isEqualTo(member2.getNickname());
		assertThat(actualChatMessageReadDtoList.get(1).profileImageUrl()).isEqualTo(member2.getProfileImageUrl());

		verify(chatRoomRepository, times(1)).findById(chatRoomId);
		verify(chatMessageRepository, times(1)).findByChatRoomOrderByCreatedAtDesc(
			any(ChatRoom.class), any(Pageable.class));
		verify(memberRepository, times(1)).findByEmailIn(anyList());
	}

	@Test
	@DisplayName("UPDATE MEMBERS IN CHAT MESSAGES(⭕️ SUCCESS): 채팅 메시지 목록의 회원 정보를 성공적으로 업데이트했습니다.")
	void updateMembersInChatMessages_void_success() {
		// GIVEN
		List<ChatMessageReadDto> chatMessages = Arrays.asList(
			new ChatMessageReadDto("member1", "Hello 👋🏻", LocalDateTime.now(), true, null),
			new ChatMessageReadDto("member2", "Bye 👋🏻", LocalDateTime.now(), true, null)
		);
		Member member1 = MemberFixture.createMemberEntityWithEmailAndNickname("member1@example.com", "member1");
		Member member2 = MemberFixture.createMemberEntityWithEmailAndNickname("member2@example.com", "member2");
		List<Member> memberList = Arrays.asList(member1, member2);

		when(memberRepository.findByEmailIn(anyList())).thenReturn(memberList);

		// WHEN
		List<ChatMessageReadDto> actualChatMessageReadDtoList =
			chatMessageReadService.updateMembersInChatMessages(chatMessages);

		// THEN
		assertThat(actualChatMessageReadDtoList).hasSize(2);
		assertThat(actualChatMessageReadDtoList.get(0).sender()).isEqualTo(member1.getNickname());
		assertThat(actualChatMessageReadDtoList.get(0).profileImageUrl()).isEqualTo(member1.getProfileImageUrl());
		assertThat(actualChatMessageReadDtoList.get(1).sender()).isEqualTo(member2.getNickname());
		assertThat(actualChatMessageReadDtoList.get(1).profileImageUrl()).isEqualTo(member2.getProfileImageUrl());
		verify(memberRepository, times(1)).findByEmailIn(anyList());
	}
}
