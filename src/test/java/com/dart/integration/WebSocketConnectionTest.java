package com.dart.integration;

import static com.dart.global.common.util.AuthConstant.*;
import static org.assertj.core.api.Assertions.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import com.dart.api.application.auth.JwtProviderService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class WebSocketConnectionTest {

	private static final String WEB_SOCKET_URL = "ws://localhost:%d/ws";

	@LocalServerPort
	private int PORT;

	@Autowired
	private JwtProviderService jwtProviderService;

	// @Test
	// @DisplayName("WEB SOCKET CONNECTION(⭕️ SUCCESS): 성공적으로 웹소켓이 연결되었습니다.")
	// public void webSocketConnection_void_success() throws ExecutionException, InterruptedException, TimeoutException {
	// 	// GIVEN
	// 	WebSocketStompClient webSocketStompClient = new WebSocketStompClient(new StandardWebSocketClient());
	//
	// 	String accessToken = jwtProviderService.generateAccessToken(
	// 		"test1@example.com",
	// 		"test1",
	// 		"testProfileImage"
	// 	);
	//
	// 	// WebSocket URL 및 헤더 설정
	// 	String URL = String.format(WEB_SOCKET_URL, PORT);
	// 	WebSocketHttpHeaders webSocketHttpHeaders = new WebSocketHttpHeaders();
	// 	webSocketHttpHeaders.add(ACCESS_TOKEN_HEADER, BEARER + " " + accessToken);
	//
	// 	// WebSocket 연결 시도
	// 	CompletableFuture<StompSession> sessionCompletableFuture = new CompletableFuture<>();
	// 	webSocketStompClient.connect(URL, webSocketHttpHeaders, new StompSessionHandlerAdapter() {
	// 		@Override
	// 		public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
	// 			sessionCompletableFuture.complete(session);
	// 		}
	//
	// 		@Override
	// 		public void handleTransportError(StompSession session, Throwable exception) {
	// 			sessionCompletableFuture.completeExceptionally(exception);
	// 		}
	// 	});
	//
	// 	// WHEN & THEN
	// 	StompSession stompSession = sessionCompletableFuture.get(5, TimeUnit.SECONDS);
	// 	assertThat(stompSession).isNotNull();
	// 	assertThat(stompSession.isConnected()).isTrue();
	// }
}
