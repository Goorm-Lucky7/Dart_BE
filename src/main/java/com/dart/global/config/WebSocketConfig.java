package com.dart.global.config;

import static com.dart.global.common.util.ChatConstant.*;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import com.dart.api.application.auth.JwtProviderService;
import com.dart.api.infrastructure.websocket.AuthHandshakeInterceptor;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	private final JwtProviderService jwtProviderService;

	@Bean
	public AuthHandshakeInterceptor authHandshakeInterceptor() {
		return new AuthHandshakeInterceptor(jwtProviderService);
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry messageBrokerRegistry) {
		messageBrokerRegistry.enableSimpleBroker(SUBSCRIPTION_PREFIX);
		messageBrokerRegistry.setApplicationDestinationPrefixes(APPLICATION_DESTINATION_PREFIX);
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry stompEndpointRegistry) {
		stompEndpointRegistry.addEndpoint(WEBSOCKET_ENDPOINT)
			.setHandshakeHandler(new DefaultHandshakeHandler())
			.setAllowedOriginPatterns(ALLOWED_ORIGIN_PATTERN);
	}

	@Override
	public void configureWebSocketTransport(WebSocketTransportRegistration webSocketTransportRegistration) {
		webSocketTransportRegistration.setMessageSizeLimit(MESSAGE_SIZE_LIMIT);
		webSocketTransportRegistration.setSendTimeLimit(SEND_TIME_LIMIT);
		webSocketTransportRegistration.setSendBufferSizeLimit(SEND_BUFFER_SIZE_LIMIT);
	}
}
