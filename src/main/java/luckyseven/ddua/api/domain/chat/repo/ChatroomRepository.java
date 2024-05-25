package luckyseven.ddua.api.domain.chat.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import luckyseven.ddua.api.domain.chat.entity.Chatroom;

public interface ChatroomRepository extends JpaRepository<Chatroom, Long> {
}
