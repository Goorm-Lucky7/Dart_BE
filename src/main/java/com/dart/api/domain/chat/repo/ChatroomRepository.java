package com.dart.api.domain.chat.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dart.api.domain.chat.entity.Chatroom;

public interface ChatroomRepository extends JpaRepository<Chatroom, Long> {
}
