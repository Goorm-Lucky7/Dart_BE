package com.dart.api.infrastructure.websocket;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MemberSessionRegistryTest {

	@InjectMocks
	private MemberSessionRegistry memberSessionRegistry;

	@Test
	@DisplayName("ADD SESSION(⭕️ SUCCESS): 사용자가 채팅방에 성공적으로 세션을 추가했습니다.")
	void addSession_void_success() {
		// GIVEN
		String memberNickname = "test1";
		String sessionId = "testSessionId";
		String chatRoomId = "1";
		String destination = "/sub/ws/" + chatRoomId;

		// WHEN
		memberSessionRegistry.addSession(memberNickname, sessionId, destination);

		// THEN
		List<String> members = memberSessionRegistry.getMembersInChatRoom(destination);
		assertThat(members).containsExactly(memberNickname);
	}

	@Test
	@DisplayName("REMOVE SESSION(⭕️ SUCCESS): 사용자가 채팅방에서 성공적으로 세션을 제거했습니다.")
	void removeSession_void_success() {
		// GIVEN
		String memberNickname = "test1";
		String sessionId = "testSessionId";
		String chatRoomId = "1";
		String destination = "/sub/ws/" + chatRoomId;

		memberSessionRegistry.addSession(memberNickname, sessionId, destination);

		// WHEN
		memberSessionRegistry.removeSession(sessionId);

		// THEN
		List<String> members = memberSessionRegistry.getMembersInChatRoom(destination);
		assertThat(members).isEmpty();
	}

	@Test
	@DisplayName("GET MEMBERS IN CHAT ROOMS(⭕️ SUCCESS): 특정 채팅방에 있는 사용자 정보 조회를 완료했습니다.")
	void getMembersInChatRoom_void_success() {
		// GIVEN
		String destination1 = "/sub/ws/1";
		String destination2 = "/sub/ws/2";

		memberSessionRegistry.addSession("member1", "sessionId1", destination1);
		memberSessionRegistry.addSession("member2", "sessionId2", destination1);
		memberSessionRegistry.addSession("member3", "sessionId3", destination2);

		// WHEN
		List<String> membersInDestination1 = memberSessionRegistry.getMembersInChatRoom(destination1);
		List<String> membersInDestination2 = memberSessionRegistry.getMembersInChatRoom(destination2);

		// THEN
		assertThat(membersInDestination1).containsExactlyInAnyOrder("member1", "member2");
		assertThat(membersInDestination2).containsExactly("member3");
	}
}
