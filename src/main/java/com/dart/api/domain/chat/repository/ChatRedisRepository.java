package com.dart.api.domain.chat.repository;

import static com.dart.global.common.util.RedisConstant.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Repository;

import com.dart.api.domain.chat.entity.ChatRoom;
import com.dart.api.dto.chat.response.ChatMessageReadDto;
import com.dart.api.dto.page.PageInfo;
import com.dart.api.dto.page.PageResponse;
import com.dart.api.infrastructure.redis.ListRedisRepository;
import com.dart.api.infrastructure.redis.ZSetRedisRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ChatRedisRepository {

	private final ListRedisRepository listRedisRepository;

	public void saveChatMessage(ChatRoom chatRoom, String content, String sender, LocalDateTime createdAt,
		long expirySeconds
	) {
		final String key = REDIS_CHAT_MESSAGE_PREFIX + chatRoom.getId();
		final boolean isAuthor = chatRoom.getGallery().getMember().getNickname().equals(sender);
		final String messageValue = createMessageValue(sender, content, createdAt, isAuthor);

		listRedisRepository.addElementWithExpiry(key, messageValue, expirySeconds);
	}

	public PageResponse<ChatMessageReadDto> getChatMessageReadDto(Long chatRoomId, int page, int size) {
		final long start = (long)page * size;
		final long end = start + size - 1;

		List<Object> messageValues = listRedisRepository.getRange(REDIS_CHAT_MESSAGE_PREFIX + chatRoomId, start, end);

		List<ChatMessageReadDto> chatMessageReadDtoList = messageValues.stream()
			.map(this::parseMessageValues)
			.toList();

		final boolean isDone = messageValues.size() < size;
		final PageInfo pageInfo = new PageInfo(page, isDone);

		return new PageResponse<>(chatMessageReadDtoList, pageInfo);
	}

	public List<ChatMessageReadDto> getAllChatMessageReadDto(Long chatRoomId) {
		List<Object> messageValues = listRedisRepository.getRange(
			REDIS_CHAT_MESSAGE_PREFIX + chatRoomId,
			LIST_START_INDEX,
			LIST_END_INDEX_ALL
		);

		return messageValues.stream()
			.map(this::parseMessageValues)
			.toList();
	}

	public void deleteChatMessages(Long chatRoomId) {
		listRedisRepository.deleteAllElements(REDIS_CHAT_MESSAGE_PREFIX + chatRoomId);
	}

	private String createMessageValue(String sender, String content, LocalDateTime createdAt, boolean isAuthor) {
		return sender + "|" + content + "|" + createdAt.toString() + "|" + isAuthor;
	}

	private ChatMessageReadDto parseMessageValues(Object messageValue) {
		final String[] parts = messageValue.toString().split("\\|");

		return ChatMessageReadDto.builder()
			.sender(parts[0])
			.content(parts[1])
			.createdAt(LocalDateTime.parse(parts[2]))
			.isAuthor(Boolean.parseBoolean(parts[3]))
			.build();
	}
}
