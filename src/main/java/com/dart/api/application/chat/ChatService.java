package com.dart.api.application.chat;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dart.api.domain.chat.entity.ChatRoom;
import com.dart.api.domain.chat.repo.ChatMessageRepository;
import com.dart.api.domain.chat.repo.ChatRoomRepository;
import com.dart.api.domain.gallery.entity.Gallery;
import com.dart.api.infrastructure.websocket.MemberSessionRegistry;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {

	private final ChatRoomRepository chatroomRepository;
	private final ChatMessageRepository chatMessageRepository;
	private final MemberSessionRegistry memberSessionRegistry;

	public void createChatRoom(Gallery gallery) {
		final ChatRoom chatRoom = ChatRoom.createChatRoom(gallery);
		chatroomRepository.save(chatRoom);
	}
}
