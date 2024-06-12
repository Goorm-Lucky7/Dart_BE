package com.dart.api.application.chat;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.dart.api.dto.chat.response.KafkaConsumerMessageDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

	private final SimpMessagingTemplate simpMessagingTemplate;

	@KafkaListener(topics = "#{spring.kafka.producer.topics}", groupId = "#{spring.kafka.consumer.group-id}")
	public void listen(KafkaConsumerMessageDto kafkaConsumerMessageDto) {
		log.info("[✅ LOGGER] RECEIVED MESSAGE: {}", kafkaConsumerMessageDto);

		simpMessagingTemplate.convertAndSend("/sub/ws/", kafkaConsumerMessageDto);
	}
}
