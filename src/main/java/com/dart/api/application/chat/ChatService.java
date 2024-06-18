package com.dart.api.application.chat;

import static com.dart.global.common.util.ChatConstant.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import com.dart.global.error.exception.ConflictException;
import com.dart.global.error.exception.NotFoundException;
import com.dart.global.error.exception.UnauthorizedException;
import com.dart.global.error.model.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatService {

	private final ChatRoomRepository chatRoomRepository;
	private final ChatMessageRepository chatMessageRepository;
	private final MemberRepository memberRepository;
	private final ChatRedisRepository chatRedisRepository;

	public void createChatRoom(Gallery gallery) {
		final ChatRoom chatRoom = ChatRoom.createChatRoom(gallery);
		chatRoomRepository.save(chatRoom);
	}

	public void deleteChatRoom(Gallery gallery) {
		final ChatRoom chatRoom = chatRoomRepository.findByGallery(gallery)
			.orElseThrow(() -> new NotFoundException(ErrorCode.FAIL_CHAT_ROOM_NOT_FOUND));

		final List<ChatMessage> chatMessages = chatMessageRepository.findByChatRoom(chatRoom);

		chatMessageRepository.deleteAll(chatMessages);
		chatRoomRepository.delete(chatRoom);
	}

	@Transactional
	public void saveChatMessage(
		Long chatRoomId,
		ChatMessageCreateDto chatMessageCreateDto,
		SimpMessageHeaderAccessor simpMessageHeaderAccessor
	) {
		final ChatRoom chatRoom = getChatRoomById(chatRoomId);
		final AuthUser authUser = extractAuthUserEmail(simpMessageHeaderAccessor);
		final Member member = getMemberByEmail(authUser.email());

		final ChatMessage chatMessage = ChatMessage.createChatMessage(chatRoom, member, chatMessageCreateDto);
		chatRedisRepository.saveChatMessage(
			chatRoom,
			chatMessage.getContent(),
			chatMessage.getSender(),
			chatMessage.getCreatedAt(),
			determineExpiry(chatRoom)
		);
	}

	@Transactional(readOnly = true)
	public List<ChatMessageReadDto> getChatMessageList(Long chatRoomId) {
		return chatRedisRepository.getChatMessageReadDto(chatRoomId);
	}

	public long determineExpiry(ChatRoom chatRoom) {
		Gallery gallery = chatRoom.getGallery();

		if ((gallery.getEndDate() == null)) {
			return FREE_MESSAGE_EXPIRY;
		}

		LocalDateTime currentDate = LocalDateTime.now();
		LocalDateTime endDate = gallery.getEndDate();

		if (endDate.isBefore(currentDate)) {
			throw new ConflictException(ErrorCode.FAIL_GALLERY_CONFLICT_ALREADY_ENDED);
		}

		return Duration.between(currentDate, endDate).getSeconds();
	}

	private AuthUser extractAuthUserEmail(SimpMessageHeaderAccessor simpMessageHeaderAccessor) {
		return (AuthUser)Objects
			.requireNonNull(simpMessageHeaderAccessor.getSessionAttributes(), "SESSION ATTRIBUTE MUST NOT BE NULL")
			.get(CHAT_SESSION_USER);
	}

	private ChatRoom getChatRoomById(Long chatRoomId) {
		return chatRoomRepository.findById(chatRoomId)
			.orElseThrow(() -> new NotFoundException(ErrorCode.FAIL_CHAT_ROOM_NOT_FOUND));
	}

	private Member getMemberByEmail(String email) {
		return memberRepository.findByEmail(email)
			.orElseThrow(() -> new UnauthorizedException(ErrorCode.FAIL_LOGIN_REQUIRED));
	}
}
