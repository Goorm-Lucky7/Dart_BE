package luckyseven.dart.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;

@Component
@Getter
public class PaymentProperties {
	@Value("${KAKAO_ADMIN_KEY}")
	private String adminKey;
}
