package com.dart.support;

import com.dart.api.dto.notification.response.NotificationReadDto;

public class NotificationFixture {

	public static NotificationReadDto createNotificationReadDto(String type) {
		return NotificationReadDto.builder()
			.message("성공적으로 SSE 연결 및 구독이 완로되었습니다.")
			.type(type)
			.build();
	}
}
