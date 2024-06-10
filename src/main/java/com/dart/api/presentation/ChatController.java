package com.dart.api.presentation;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.RestController;

import com.dart.api.application.chat.ChatService;
import com.dart.api.dto.chat.request.ChatMessageCreateDto;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ChatController {

	private final ChatService chatService;
	private final SimpMessageSendingOperations simpMessageSendingOperations;

	@MessageMapping(value = "/ws/{chat-room-id}/chat-messages")
	public void saveAndSendChatMessage(
		@DestinationVariable("chat-room-id") Long chatRoomId,
		@Payload ChatMessageCreateDto chatMessageCreateDto,
		SimpMessageHeaderAccessor simpMessageHeaderAccessor
	) {
		chatService.saveAndSendChatMessage(chatRoomId, chatMessageCreateDto, simpMessageHeaderAccessor);
		simpMessageSendingOperations.convertAndSend("/sub/ws/" + chatRoomId, chatMessageCreateDto.content());
	}
}
