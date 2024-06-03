package com.dart.api.domain.chat.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dart.api.domain.chat.entity.Message;

public interface MessageRepository extends JpaRepository<Message, Long> {
}
