package com.dart.global.common.util;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface ClockHolder {
	LocalDate nowDate();

	LocalDateTime minusOneDaysAtTime(LocalDate date);
}
