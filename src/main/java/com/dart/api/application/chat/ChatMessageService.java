package com.dart.api.application.chat;

import static com.dart.global.common.util.ChatConstant.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import com.dart.global.error.model.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

	private final ChatRoomRepository chatRoomRepository;
	private final MemberRepository memberRepository;
	private final ChatRedisRepository chatRedisRepository;
	private final ChatMessageRepository chatMessageRepository;

	@Transactional
	public void saveChatMessage(Long chatRoomId, ChatMessageCreateDto chatMessageCreateDto) {
		final ChatRoom chatRoom = getChatRoomById(chatRoomId);
		final Member member = getMemberByNickname(chatMessageCreateDto.sender());
		final ChatMessage chatMessage = ChatMessage.chatMessageFromCreateDto(chatRoom, member, chatMessageCreateDto);
		chatMessageRepository.save(chatMessage);

		final ChatMessageSendDto chatMessageSendDto = chatMessage.toChatMessageSendDto(CHAT_MESSAGE_EXPIRY_SECONDS);
		chatRedisRepository.saveChatMessage(chatMessageSendDto, member);
	}

	private ChatRoom getChatRoomById(Long chatRoomId) {
		return chatRoomRepository.findById(chatRoomId)
			.orElseThrow(() -> new NotFoundException(ErrorCode.FAIL_CHAT_ROOM_NOT_FOUND));
	}

	private Member getMemberByNickname(String nickname) {
		return memberRepository.findByNickname(nickname)
			.orElseThrow(() -> new NotFoundException(ErrorCode.FAIL_MEMBER_NOT_FOUND));
	}
}
