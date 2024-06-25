package com.dart.api.domain.chat.repository;

import static com.dart.global.common.util.RedisConstant.*;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.dart.api.domain.chat.entity.ChatRoom;
import com.dart.api.dto.chat.response.ChatMessageReadDto;
import com.dart.api.dto.page.PageInfo;
import com.dart.api.dto.page.PageResponse;
import com.dart.api.infrastructure.redis.ListRedisRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ChatRedisRepository {

	private final ListRedisRepository listRedisRepository;
	private final ObjectMapper objectMapper;

	public void saveChatMessage(ChatRoom chatRoom, String content, String sender, LocalDateTime createdAt,
		long expirySeconds, String profileImageUrl
	) {
		final String key = REDIS_CHAT_MESSAGE_PREFIX + chatRoom.getId();
		final boolean isAuthor = chatRoom.getGallery().getMember().getNickname().equals(sender);

		ChatMessageReadDto chatMessageReadDto =
			new ChatMessageReadDto(sender, content, createdAt, isAuthor, profileImageUrl);
		String messageValue = convertToJson(chatMessageReadDto);

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

	private String convertToJson(ChatMessageReadDto chatMessageReadDto) {
		try {
			return objectMapper.writeValueAsString(chatMessageReadDto);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	private ChatMessageReadDto parseMessageValues(Object messageValue) {
		try {
			return objectMapper.readValue(messageValue.toString(), ChatMessageReadDto.class);
		} catch (JsonMappingException e) {
			throw new RuntimeException("Failed to map JSON to ChatMessageReadDto", e);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Failed to process JSON", e);
		}
	}
}
