package com.dart.global.common.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.stereotype.Component;

@Component
public class ClockHolderImpl implements ClockHolder {

	@Override
	public LocalDate nowDate() {
		return LocalDate.now();
	}

	@Override
	public LocalDateTime minusOneDaysAtTime(LocalDate date) {
		return date.minusDays(1).atTime(LocalTime.MAX);
	}
}
