package com.dart.api.domain.chat.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dart.api.domain.chat.entity.ChatMessage;
import com.dart.api.domain.chat.entity.ChatRoom;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

	List<ChatMessage> findByChatRoom(ChatRoom chatRoom);
}
