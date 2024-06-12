package com.dart.global.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import com.dart.api.dto.chat.response.KafkaConsumerMessageDto;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {

	private static final String GROUP_ID = "chat-group";
	private static final String AUTO_OFFSET_RESET = "earliest";
	private static final String TRUSTED_PACKAGES = "*";

	@Value("${kafka.bootstrap-servers}")
	private String bootstrapServers;

	@Bean
	public ConsumerFactory<String, KafkaConsumerMessageDto> consumerFactory() {
		JsonDeserializer<KafkaConsumerMessageDto> jsonDeserializer =
			new JsonDeserializer<>(KafkaConsumerMessageDto.class);
		jsonDeserializer.addTrustedPackages(TRUSTED_PACKAGES);

		Map<String, Object> configProps = new HashMap<>();
		configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		configProps.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
		configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, jsonDeserializer);
		// EARLIEST: 최초 데이터부터, LATEST: 최신 데이터부터, NONE: 이전 오프셋이 없다면 오류
		configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, AUTO_OFFSET_RESET);

		return new DefaultKafkaConsumerFactory<>(configProps, new StringDeserializer(), jsonDeserializer);
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, KafkaConsumerMessageDto> kafkaListenerContainerFactory() {
		ConcurrentKafkaListenerContainerFactory<String, KafkaConsumerMessageDto> concurrentKafkaListenerContainerFactory =
			new ConcurrentKafkaListenerContainerFactory<>();

		concurrentKafkaListenerContainerFactory.setConsumerFactory(consumerFactory());

		return concurrentKafkaListenerContainerFactory;
	}
}
