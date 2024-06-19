package com.dart.api.application.chat;

import static com.dart.global.common.util.ChatConstant.*;

import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dart.api.domain.auth.entity.AuthUser;
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
import com.dart.global.error.exception.UnauthorizedException;
import com.dart.global.error.model.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

	// TODO: 상수로 정의한 값은 ChatConstant로 이동
	// TODO: 이제 RedisPublish와 RedisSubscribe를 구현을 해서 레디스를 사용한 PUB/SUB 방식을 구현해야 함.
	// TODO: 우선은 현재까지 진행한 내용을 PR 올리고 두 번째 TODO는 새롭게 브랜치를 만들어서 구현을 하자.

	public final static String SORT_FIELD_CREATED_AT = "createdAt";

	private final ChatRoomRepository chatRoomRepository;
	private final MemberRepository memberRepository;
	private final ChatRedisRepository chatRedisRepository;
	private final ChatMessageRepository chatMessageRepository;

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

		chatMessageRepository.save(chatMessage);

		final long expirySeconds = 60 * 60;
		chatRedisRepository.saveChatMessage(chatRoom, chatMessage.getContent(), chatMessage.getSender(),
			chatMessage.getCreatedAt(), expirySeconds);
	}

	@Transactional(readOnly = true)
	public PageResponse<ChatMessageReadDto> getChatMessageList(Long chatRoomId, int page, int size) {

		// 1. Redis에서 메시지 조회
		PageResponse<ChatMessageReadDto> redisChatMessageReadDtoList =
			chatRedisRepository.getChatMessageReadDto(chatRoomId, page, size);

		// 2. Redis에서 메시지가 없는 경우
		if (redisChatMessageReadDtoList.pages().isEmpty()) {
			return redisChatMessageReadDtoList;
		}

		final ChatRoom chatRoom = getChatRoomById(chatRoomId);

		Pageable pageable = PageRequest.of(page, size, Sort.by(SORT_FIELD_CREATED_AT).ascending());
		Page<ChatMessage> mySQLChatMessages = chatMessageRepository.findByChatRoom(chatRoom, pageable);

		// 3. MySQL에서 조회된 메시지를 Redis에 저장
		List<ChatMessageReadDto> mySQLChatMessageReadDtoList = mySQLChatMessages.stream()
			.map(ChatMessage::getChatMessageReadDto)
			.toList();

		if (!mySQLChatMessageReadDtoList.isEmpty()) {
			// TODO: 쿼리 어노테이션을 사용해서 chatMessageRepository에서 조회를 하는 방법도 고려
			mySQLChatMessageReadDtoList.forEach(
				chatMessages -> chatRedisRepository.saveChatMessage(
					chatRoom, chatMessages.content(), chatMessages.sender(), chatMessages.createdAt(), 60 * 60
				)
			);
		}

		// 4. 페이징 정보 설정 및 반환
		final boolean isDone = mySQLChatMessages.getNumberOfElements() < size;
		final PageInfo pageInfo = new PageInfo(page, isDone);

		return new PageResponse<>(mySQLChatMessageReadDtoList, pageInfo);
	}

	private AuthUser extractAuthUserEmail(SimpMessageHeaderAccessor simpMessageHeaderAccessor) {
		return (AuthUser)Objects.requireNonNull(simpMessageHeaderAccessor.getSessionAttributes())
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
