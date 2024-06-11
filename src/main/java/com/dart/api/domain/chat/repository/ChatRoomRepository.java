package com.dart.api.domain.chat.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dart.api.domain.chat.entity.ChatRoom;
import com.dart.api.domain.gallery.entity.Gallery;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

	Optional<ChatRoom> findByGallery(Gallery gallery);
}
