package com.dart.api.domain.notification.repository;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dart.api.dto.notification.response.NotificationReadDto;

@ExtendWith(MockitoExtension.class)
class PendingEventsRepositoryTest {

	@InjectMocks
	private PendingEventsRepository pendingEventsRepository;

	@Test
	@DisplayName("SAVE PENDING EVENT CACHE(⭕️ SUCCESS): 성공적으로 대기 중인 이벤트를 캐시에 저장했습니다.")
	void savePendingEventCache_void_success() {
		// GIVEN
		Long clientID = 1L;

		NotificationReadDto notificationReadDto1 = new NotificationReadDto("event-1", "message1", "type1");
		NotificationReadDto notificationReadDto2 = new NotificationReadDto("event-2", "message2", "type2");

		// WHEN
		pendingEventsRepository.savePendingEventCache(clientID, notificationReadDto1);
		pendingEventsRepository.savePendingEventCache(clientID, notificationReadDto2);

		// THEN
		List<NotificationReadDto> pendingEventsReadDtoList = pendingEventsRepository.getPendingEvents(clientID);
		assertThat(pendingEventsReadDtoList).hasSize(2);
		assertThat(pendingEventsReadDtoList).contains(notificationReadDto1, notificationReadDto2);
	}

	@Test
	@DisplayName("GET PENDING EVENTS(⭕️ SUCCESS): 성공적으로 대기 중인 이벤트를 캐시에서 조회했습니다.")
	void getPendingEvents_void_success() {
		// GIVEN
		Long clientID = 1L;

		NotificationReadDto notificationReadDto1 = new NotificationReadDto("event-1", "message1", "type1");
		NotificationReadDto notificationReadDto2 = new NotificationReadDto("event-2", "message2", "type2");

		pendingEventsRepository.savePendingEventCache(clientID, notificationReadDto1);
		pendingEventsRepository.savePendingEventCache(clientID, notificationReadDto2);

		// WHEN
		List<NotificationReadDto> pendingEventsReadDtoList = pendingEventsRepository.getPendingEvents(clientID);

		// THEN
		assertThat(pendingEventsReadDtoList).hasSize(2);
		assertThat(pendingEventsReadDtoList).contains(notificationReadDto1, notificationReadDto2);
	}

	@Test
	@DisplayName("GET PENDING EVENTS CLIENT NOT EXIST(❌️ FAILURE): 사용자가 존재하지 않아 대기 중인 이벤트 조회에 실패했습니다.")
	void getPendingEvents_clientNotExist_fail() {
		// GIVEN
		Long clientID = 999L;

		// WHEN
		List<NotificationReadDto> pendingEventsReadDtoList = pendingEventsRepository.getPendingEvents(clientID);

		// THEN
		assertThat(pendingEventsReadDtoList).isEmpty();
	}

	@Test
	@DisplayName("CLEAR PENDING EVENTS(⭕️ SUCCESS): 성공적으로 대기 중인 이벤트를 캐시에서 삭제했습니다.")
	void clearPendingEvents_void_success() {
		// GIVEN
		Long clientID = 1L;

		NotificationReadDto notificationReadDto1 = new NotificationReadDto("event-1", "message1", "type1");
		NotificationReadDto notificationReadDto2 = new NotificationReadDto("event-2", "message2", "type2");

		pendingEventsRepository.savePendingEventCache(clientID, notificationReadDto1);
		pendingEventsRepository.savePendingEventCache(clientID, notificationReadDto2);

		// WHEN
		pendingEventsRepository.clearPendingEvents(clientID);

		// THEN
		List<NotificationReadDto> pendingEventsReadDtoList = pendingEventsRepository.getPendingEvents(clientID);
		assertThat(pendingEventsReadDtoList).isEmpty();
	}
}
