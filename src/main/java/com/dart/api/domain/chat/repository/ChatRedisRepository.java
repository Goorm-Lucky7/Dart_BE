package com.dart.api.domain.chat.repository;

import static com.dart.global.common.util.RedisConstant.*;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.dart.api.domain.chat.entity.ChatMessage;
import com.dart.api.domain.member.entity.Member;
import com.dart.api.dto.chat.request.ChatMessageSendDto;
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

	public void saveChatMessage(ChatMessageSendDto chatMessageSendDto, Member member) {
		final String key = REDIS_CHAT_MESSAGE_PREFIX + chatMessageSendDto.chatRoomId();
		final ChatMessageReadDto chatMessageReadDto = createChatMessageReadDto(chatMessageSendDto, member);
		final String messageValue = convertToJson(chatMessageReadDto);

		listRedisRepository.addElementWithExpiry(key, messageValue, chatMessageSendDto.expirySeconds());
		listRedisRepository.addElement(REDIS_BATCH_PREFIX, messageValue);
	}

	public PageResponse<ChatMessageReadDto> getChatMessageReadDto(Long chatRoomId, int page, int size) {
		final long end = -1 - ((long)page * size);
		final long start = end - size + 1;

		final List<Object> messageValues = listRedisRepository
			.getRange(REDIS_CHAT_MESSAGE_PREFIX + chatRoomId, start, end);

		final List<ChatMessageReadDto> chatMessageReadDtoList = messageValues.stream()
			.map(this::parseMessageValues)
			.toList();

		final boolean isDone = messageValues.size() < size;
		final PageInfo pageInfo = new PageInfo(page, isDone);

		return new PageResponse<>(chatMessageReadDtoList, pageInfo);
	}

	public List<ChatMessage> getAllBatchMessages() {
		return listRedisRepository.getRange(REDIS_BATCH_PREFIX, 0, -1).stream()
			.map(this::parseChatMessage)
			.toList();
	}

	public void deleteChatMessages(Long chatRoomId) {
		listRedisRepository.deleteAllElements(REDIS_CHAT_MESSAGE_PREFIX + chatRoomId);
	}

	public void clearBatchMessages() {
		listRedisRepository.deleteAllElements(REDIS_BATCH_PREFIX);
	}

	private ChatMessageReadDto createChatMessageReadDto(ChatMessageSendDto chatMessageSendDto, Member member) {
		return new ChatMessageReadDto(
			member.getNickname(), chatMessageSendDto.content(), chatMessageSendDto.createdAt(),
			chatMessageSendDto.isAuthor(), member.getProfileImageUrl());
	}

	private String convertToJson(Object object) {
		try {
			return objectMapper.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	private ChatMessage parseChatMessage(Object messageValue) {
		try {
			return objectMapper.readValue(messageValue.toString(), ChatMessage.class);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("[✅ LOGGER] FAILED TO PROCESS JSON", e);
		}
	}

	private ChatMessageReadDto parseMessageValues(Object messageValue) {
		try {
			return objectMapper.readValue(messageValue.toString(), ChatMessageReadDto.class);
		} catch (JsonMappingException e) {
			throw new RuntimeException("[✅ LOGGER] FAILED TO MAP JSON TO CHAT MESSAGE READ DTO", e);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("[✅ LOGGER] FAILED TO PROCESS JSON", e);
		}
	}
}
