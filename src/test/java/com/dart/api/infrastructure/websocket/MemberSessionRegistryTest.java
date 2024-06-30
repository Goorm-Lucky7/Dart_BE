package com.dart.api.infrastructure.websocket;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dart.api.dto.chat.response.MemberSessionDto;

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
		String profileImageUrl = "https://example.com/profile.jpg";

		// WHEN
		memberSessionRegistry.addSession(memberNickname, sessionId, destination, profileImageUrl);

		// THEN
		List<MemberSessionDto> members = memberSessionRegistry.getMembersInChatRoom(destination);
		assertThat(members).hasSize(1);
		MemberSessionDto memberSessionDto = members.get(0);
		assertThat(memberSessionDto.nickname()).isEqualTo(memberNickname);
		assertThat(memberSessionDto.profileImageUrl()).isEqualTo(profileImageUrl);
	}

	@Test
	@DisplayName("REMOVE SESSION(⭕️ SUCCESS): 사용자가 채팅방에서 성공적으로 세션을 제거했습니다.")
	void removeSession_void_success() {
		// GIVEN
		String memberNickname = "test1";
		String sessionId = "testSessionId";
		String chatRoomId = "1";
		String destination = "/sub/ws/" + chatRoomId;
		String profileImageUrl = "https://example.com/profile.jpg";

		memberSessionRegistry.addSession(memberNickname, sessionId, destination, profileImageUrl);

		// WHEN
		memberSessionRegistry.removeSession(sessionId);

		// THEN
		List<MemberSessionDto> members = memberSessionRegistry.getMembersInChatRoom(destination);
		assertThat(members).isEmpty();
	}

	@Test
	@DisplayName("GET MEMBERS IN CHAT ROOMS(⭕️ SUCCESS): 특정 채팅방에 있는 사용자 정보 조회를 완료했습니다.")
	void getMembersInChatRoom_void_success() {
		// GIVEN
		String destination1 = "/sub/ws/1";
		String destination2 = "/sub/ws/2";
		String profileImageURL1 = "https://example.com/profile1.jpg";
		String profileImageURL2 = "https://example.com/profile2.jpg";
		String profileImageURL3 = "https://example.com/profile3.jpg";

		memberSessionRegistry.addSession("member1", "sessionId1", destination1, profileImageURL1);
		memberSessionRegistry.addSession("member2", "sessionId2", destination1, profileImageURL2);
		memberSessionRegistry.addSession("member3", "sessionId3", destination2, profileImageURL3);

		// WHEN
		List<MemberSessionDto> membersInDestination1 = memberSessionRegistry.getMembersInChatRoom(destination1);
		List<MemberSessionDto> membersInDestination2 = memberSessionRegistry.getMembersInChatRoom(destination2);

		// THEN
		assertThat(membersInDestination1).hasSize(2);
		assertThat(membersInDestination1).extracting("nickname")
			.containsExactlyInAnyOrder("member1", "member2");
		assertThat(membersInDestination1).extracting("profileImageUrl")
			.containsExactlyInAnyOrder(profileImageURL1, profileImageURL2);

		assertThat(membersInDestination2).hasSize(1);
		assertThat(membersInDestination2.get(0).nickname()).isEqualTo("member3");
		assertThat(membersInDestination2.get(0).profileImageUrl()).isEqualTo(profileImageURL3);
	}
}
