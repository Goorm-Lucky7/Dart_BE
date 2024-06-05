package com.dart.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
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
		messageBrokerRegistry.enableSimpleBroker("/sub");
		messageBrokerRegistry.setApplicationDestinationPrefixes("/pub");
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry stompEndpointRegistry) {
		stompEndpointRegistry.addEndpoint("/ws")
			.setHandshakeHandler(new DefaultHandshakeHandler())
			.addInterceptors(authHandshakeInterceptor())
			.setAllowedOriginPatterns("*");
	}
}
