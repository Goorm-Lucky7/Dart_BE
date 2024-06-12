package com.dart.api.application.chat;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.dart.api.dto.chat.request.KafkaProducerMessageDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KafkaProducerService {

	private final KafkaTemplate<String, KafkaProducerMessageDto> kafkaTemplate;

	public void sendMessage(String topic, KafkaProducerMessageDto kafkaProducerMessageDto) {
		kafkaTemplate.send(topic, kafkaProducerMessageDto);
	}
}
