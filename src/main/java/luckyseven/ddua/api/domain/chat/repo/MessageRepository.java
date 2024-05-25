package luckyseven.ddua.api.domain.chat.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import luckyseven.ddua.api.domain.chat.entity.Message;

public interface MessageRepository extends JpaRepository<Message, Long> {
}
