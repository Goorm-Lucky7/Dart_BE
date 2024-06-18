package com.dart.api.application.chat;

import static com.dart.global.common.util.ChatConstant.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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
import com.dart.api.domain.chat.entity.ChatRoom;
import com.dart.api.domain.chat.repository.ChatRedisRepository;
import com.dart.api.domain.chat.repository.ChatRoomRepository;
import com.dart.api.domain.gallery.entity.Gallery;
import com.dart.api.domain.member.entity.Member;
import com.dart.api.domain.member.repository.MemberRepository;
import com.dart.api.dto.chat.request.ChatMessageCreateDto;
import com.dart.api.dto.chat.response.ChatMessageReadDto;
import com.dart.global.error.exception.ConflictException;
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

		Map<String, Object> sessionAttributes = new HashMap<>();
		sessionAttributes.put(CHAT_SESSION_USER, authUser);

		given(chatRoomRepository.findById(chatRoomId)).willReturn(Optional.of(chatRoom));
		given(memberRepository.findByEmail(memberEmail)).willReturn(Optional.of(member));
		given(simpMessageHeaderAccessor.getSessionAttributes()).willReturn(sessionAttributes);

		// WHEN
		chatMessageService.saveChatMessage(chatRoomId, chatMessageCreateDto, simpMessageHeaderAccessor);

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
			() -> chatMessageService.saveChatMessage(chatRoomId, chatMessageCreateDto, simpMessageHeaderAccessor))
			.isInstanceOf(NotFoundException.class)
			.hasMessage("[❎ ERROR] 요청하신 채팅방을 찾을 수 없습니다.");

		verify(chatRoomRepository, times(1)).findById(chatRoomId);
		verify(memberRepository, times(0)).findByEmail(any(String.class));
		verify(chatRedisRepository, times(0))
			.saveChatMessage(any(ChatRoom.class), anyString(), anyString(), any(LocalDateTime.class), anyLong());
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
		verify(chatRedisRepository, times(0))
			.saveChatMessage(any(ChatRoom.class), anyString(), anyString(), any(LocalDateTime.class), anyLong());
	}

	@Test
	@DisplayName("GET CHAT MESSAGE LIST(⭕️ SUCCESS): 성공적으로 채팅 메시지 목록을 조회했습니다.")
	void getChatMessageList_void_success() {
		// GIVEN
		Member member = MemberFixture.createMemberEntity();
		Member author = MemberFixture.createMemberEntityForAuthor();

		Gallery gallery = GalleryFixture.createGalleryEntityForAuthor();
		ChatRoom chatRoom = ChatFixture.createChatRoomEntity(gallery);
		Long chatRoomId = chatRoom.getId();

		when(chatRedisRepository.getChatMessageReadDto(chatRoomId)).thenReturn(
			List.of(
				new ChatMessageReadDto(member.getNickname(), "Hello 👋🏻", LocalDateTime.now(), false),
				new ChatMessageReadDto(author.getNickname(), "Have a good time 👏", LocalDateTime.now(), false)
			)
		);

		// WHEN
		List<ChatMessageReadDto> actualMessages = chatMessageService.getChatMessageList(chatRoomId);

		// THEN
		assertEquals(2, actualMessages.size());

		assertEquals(member.getNickname(), actualMessages.get(0).sender());
		assertEquals("Hello 👋🏻", actualMessages.get(0).content());
		assertFalse(actualMessages.get(0).isAuthor());

		assertEquals(author.getNickname(), actualMessages.get(1).sender());
		assertEquals("Have a good time 👏", actualMessages.get(1).content());
		assertFalse(actualMessages.get(1).isAuthor());
	}

	@Test
	@DisplayName("GET CHAT MESSAGE LIST(❌ FAILURE): 조회된 채팅 메시지가 없을 때 빈 리스트를 반환합니다.")
	void getChatMessageList_empty_fail() {
		// GIVEN
		Long chatRoomId = 1L;

		when(chatRedisRepository.getChatMessageReadDto(chatRoomId)).thenReturn(List.of());

		// WHEN
		List<ChatMessageReadDto> actualMessages = chatMessageService.getChatMessageList(chatRoomId);

		// THEN
		assertTrue(actualMessages.isEmpty());
	}

	@Test
	@DisplayName("DETERMINE EXPIRY FREE GALLERY(⭕️ SUCCESS): 종료 날짜가 없는 무료 전시회의 채팅 메시지 만료 시간을 설정하지 않았습니다.")
	void determineExpiry_free_success() {
		// GIVEN
		Gallery gallery = GalleryFixture.createFreeGalleryEntity();
		ChatRoom chatRoom = ChatFixture.createChatRoomEntity(gallery);

		// WHEN
		long actualExpiry = chatMessageService.determineExpiry(chatRoom);

		// THEN
		assertThat(actualExpiry).isEqualTo(FREE_MESSAGE_EXPIRY);
	}

	@Test
	@DisplayName("DETERMINE EXPIRY PAID GALLERY(⭕️ SUCCESS): 종료 날짜가 있는 유료 전시회의 채팅 메시지 만료 시간을 설정합니다.")
	void determineExpiry_paid_success() {
		// GIVEN
		LocalDateTime currentDate = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
		LocalDateTime endDate = currentDate.plusDays(5);
		long expectedExpiry = Duration.between(currentDate, endDate).getSeconds();

		Gallery gallery = GalleryFixture.createPaidGalleryEntity(currentDate, endDate);
		ChatRoom chatRoom = ChatFixture.createChatRoomEntity(gallery);

		// WHEN
		long actualExpiry = chatMessageService.determineExpiry(chatRoom);

		// THEN
		assertThat(actualExpiry).isCloseTo(expectedExpiry, within(1L));
	}

	@Test
	@DisplayName("DETERMINE EXPIRY PAID GALLERY(❌ FAILURE): 과거에 종료된 유료 전시회의 채팅 메시지 만료 시간을 설정할 수 없습니다.")
	void determineExpiry_paid_ConflictException_fail() {
		// GIVEN
		LocalDateTime currentDate = LocalDateTime.now();
		Gallery gallery = GalleryFixture.createPaidGalleryEntity(currentDate.minusDays(2), currentDate.minusDays(1));
		ChatRoom chatRoom = ChatFixture.createChatRoomEntity(gallery);

		// WHEN & THEN
		assertThatThrownBy(() -> chatMessageService.determineExpiry(chatRoom))
			.isInstanceOf(ConflictException.class)
			.hasMessage("[❎ ERROR] 해당 전시회는 이미 종료된 전시회입니다.");
	}
}
