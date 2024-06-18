package com.dart.api.domain.chat.repository;

import static com.dart.global.common.util.RedisConstant.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashSet;
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
		String key = REDIS_CHAT_MESSAGE_PREFIX + chatRoom.getId();
		boolean isAuthor = chatRoom.getGallery().getMember().getNickname().equals(sender);
		String messageValue = createMessageValue(sender, content, createdAt, isAuthor);

		addElementToRedis(key, messageValue, createdAt, expirySeconds);
	}

	public List<ChatMessageReadDto> getChatMessageReadDto(Long chatRoomId) {
		Set<Object> messageValues = new LinkedHashSet<>(
			zSetRedisRepository.getRange(REDIS_CHAT_MESSAGE_PREFIX + chatRoomId, 0, -1)
		);

		Set<Object> validateMessageValues = validateMessageValuesIfAbsent(messageValues);

		return validateMessageValues.stream()
			.map(this::parseMessageValues)
			.toList();
	}

	public void deleteChatMessages(Long chatRoomId) {
		zSetRedisRepository.deleteAllElements(REDIS_CHAT_MESSAGE_PREFIX + chatRoomId);
	}

	private void addElementToRedis(String key, String messageValue, LocalDateTime createdAt, long expirySeconds) {
		zSetRedisRepository.addElementIfAbsent(key, messageValue, createdAt.toEpochSecond(ZoneOffset.UTC),
			expirySeconds);

		if (expirySeconds > 0) {
			zSetRedisRepository.addElement(key, messageValue, createdAt.toEpochSecond(ZoneOffset.UTC));
		}
	}

	private String createMessageValue(String sender, String content, LocalDateTime createdAt, boolean isAuthor) {
		return sender + "|" + content + "|" + createdAt.toString() + "|" + isAuthor;
	}

	private Set<Object> validateMessageValuesIfAbsent(Set<Object> messageValues) {
		if (messageValues == null || messageValues.isEmpty()) {
			return new LinkedHashSet<>();
		}

		return messageValues;
	}

	private ChatMessageReadDto parseMessageValues(Object messageValue) {
		String[] parts = messageValue.toString().split("\\|");

		return ChatMessageReadDto.builder()
			.sender(parts[0])
			.content(parts[1])
			.createdAt(LocalDateTime.parse(parts[2]))
			.isAuthor(Boolean.parseBoolean(parts[3]))
			.build();
	}
}
