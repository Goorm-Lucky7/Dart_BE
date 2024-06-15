package com.dart.api.domain.chat.repository;

import static com.dart.api.infrastructure.redis.RedisConstant.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Repository;

import com.dart.api.domain.chat.entity.ChatRoom;
import com.dart.api.dto.chat.response.ChatMessageReadDto;
import com.dart.api.infrastructure.redis.ZSetRedisRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ChatRedisRepository {

	private final ZSetRedisRepository zSetRedisRepository;

	public void saveChatMessage(ChatRoom chatRoom, String content, String sender, LocalDateTime createdAt,
		long expirySeconds) {
		String messageValue = createMessageValue(sender, content, createdAt);

		zSetRedisRepository.addElementIfAbsent(
			REDIS_CHAT_MESSAGE_PREFIX + chatRoom.getId(),
			messageValue,
			createdAt.toEpochSecond(ZoneOffset.UTC),
			expirySeconds
		);
	}

	public List<ChatMessageReadDto> getChatMessageReadDto(Long chatRoomId) {
		Set<Object> messageValues = zSetRedisRepository.getRange(REDIS_CHAT_MESSAGE_PREFIX + chatRoomId, 0, -1);

		Set<Object> validateMessageValues = validateMessageValuesIfAbsent(messageValues);

		return validateMessageValues.stream()
			.map(this::parseMessageValues)
			.toList();
	}

	public void deleteChatMessages(Long chatRoomId) {
		zSetRedisRepository.deleteAllElements(REDIS_CHAT_MESSAGE_PREFIX + chatRoomId);
	}

	public void deleteChatMessage(Long chatRoomId, String content, String sender, LocalDateTime createdAt) {
		String messageValue = createMessageValue(sender, content, createdAt);

		zSetRedisRepository.removeElement(REDIS_CHAT_MESSAGE_PREFIX + chatRoomId, messageValue);
	}

	private String createMessageValue(String sender, String content, LocalDateTime createdAt) {
		return sender + "|" + content + "|" + createdAt.toString();
	}

	private Set<Object> validateMessageValuesIfAbsent(Set<Object> messageValues) {
		if (messageValues == null || messageValues.isEmpty()) {
			return Set.of();
		}

		return messageValues;
	}

	private ChatMessageReadDto parseMessageValues(Object messageValue) {
		String[] parts = messageValue.toString().split("\\|");

		return ChatMessageReadDto.builder()
			.sender(parts[0])
			.content(parts[1])
			.createdAt(LocalDateTime.parse(parts[2]))
			.build();
	}
}
