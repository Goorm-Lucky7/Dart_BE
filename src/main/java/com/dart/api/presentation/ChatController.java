package com.dart.api.presentation;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dart.api.application.chat.ChatMessageService;
import com.dart.api.dto.chat.request.ChatMessageCreateDto;
import com.dart.api.dto.chat.response.ChatMessageReadDto;
import com.dart.api.dto.page.PageResponse;
import com.dart.api.infrastructure.websocket.MemberSessionRegistry;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ChatController {

	private final ChatMessageService chatMessageService;
	private final MemberSessionRegistry memberSessionRegistry;

	@MessageMapping(value = "/ws/{chat-room-id}/chat-messages")
	public void saveAndSendChatMessage(
		@DestinationVariable("chat-room-id") Long chatRoomId,
		@Payload ChatMessageCreateDto chatMessageCreateDto,
		SimpMessageHeaderAccessor simpMessageHeaderAccessor
	) {
		chatMessageService.saveChatMessage(chatRoomId, chatMessageCreateDto, simpMessageHeaderAccessor);
	}

	@GetMapping("/api/chat-rooms/{chat-room-id}/members")
	public ResponseEntity<List<String>> getLoggedInVisitors(@PathVariable("chat-room-id") Long chatRoomId) {
		return ResponseEntity.ok(memberSessionRegistry.getMembersInChatRoom("/sub/ws/" + chatRoomId));
	}

	@GetMapping("/api/{chat-room-id}/chat-messages")
	public ResponseEntity<PageResponse<ChatMessageReadDto>> getChatMessageList(
		@PathVariable("chat-room-id") Long chatRoomId,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "99") int size
	) {
		return ResponseEntity.ok(chatMessageService.getChatMessageList(chatRoomId, page, size));
	}
}
