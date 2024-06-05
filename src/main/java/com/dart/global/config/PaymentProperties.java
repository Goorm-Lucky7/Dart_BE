package com.dart.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;

@Component
@Getter
public class PaymentProperties {
	@Value("${kakao.admin-key}")
	private String adminKey;
}
