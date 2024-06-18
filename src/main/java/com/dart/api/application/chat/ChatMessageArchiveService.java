package com.dart.api.application.chat;

import static com.dart.global.common.util.RedisConstant.*;

import java.util.List;

import org.springframework.stereotype.Service;

import com.dart.api.domain.chat.entity.ChatMessage;
import com.dart.api.domain.chat.entity.ChatRoom;
import com.dart.api.domain.chat.repository.ChatMessageRepository;
import com.dart.api.domain.chat.repository.ChatRedisRepository;
import com.dart.api.domain.chat.repository.ChatRoomRepository;
import com.dart.api.dto.chat.response.ChatMessageReadDto;
import com.dart.global.error.exception.BadRequestException;
import com.dart.global.error.exception.NotFoundException;
import com.dart.global.error.model.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatMessageArchiveService {

	private final ChatMessageRepository chatMessageRepository;
	private final ChatRedisRepository chatRedisRepository;
	private final ChatRoomRepository chatRoomRepository;

	public void handleRedisExpiredEvent(String key) {
		validateStartWithKey(key);

		final Long chatRoomId = extractChatRoomIdFromKey(key);
		final List<ChatMessageReadDto> expiredMessages = chatRedisRepository.getChatMessageReadDto(chatRoomId);

		saveExpiredMessages(chatRoomId, expiredMessages);
		chatRedisRepository.deleteChatMessages(chatRoomId);
	}

	private void saveExpiredMessages(Long chatRoomId, List<ChatMessageReadDto> chatMessageReadDtoList) {
		final ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
			.orElseThrow(() -> new NotFoundException(ErrorCode.FAIL_CHAT_ROOM_NOT_FOUND));

		List<ChatMessage> chatMessages = chatMessageReadDtoList.stream()
			.map(chatMessage -> ChatMessage.createChatMessageFromChatMessageReadDto(chatMessage, chatRoom))
			.toList();

		chatMessageRepository.saveAll(chatMessages);
	}

	private Long extractChatRoomIdFromKey(String key) {
		return Long.parseLong(key.replace(REDIS_CHAT_MESSAGE_PREFIX, ""));
	}

	private void validateStartWithKey(String key) {
		if (!key.startsWith(REDIS_CHAT_MESSAGE_PREFIX)) {
			throw new BadRequestException(ErrorCode.FAIL_INVALID_CHAT_ROOM_KEY);
		}
	}
}
