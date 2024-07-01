package com.dart.integration;

import static com.dart.global.common.util.AuthConstant.*;

import java.lang.reflect.Type;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import com.dart.api.application.auth.JwtProviderService;
import com.dart.api.domain.chat.entity.ChatRoom;
import com.dart.api.domain.chat.repository.ChatRoomRepository;
import com.dart.api.domain.gallery.entity.Gallery;
import com.dart.api.domain.gallery.repository.GalleryRepository;
import com.dart.api.domain.member.entity.Member;
import com.dart.api.domain.member.repository.MemberRepository;
import com.dart.support.ChatFixture;
import com.dart.support.GalleryFixture;
import com.dart.support.MemberFixture;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class WebSocketMessageTest {

	private static final String WEB_SOCKET_URL = "ws://localhost:%d/ws";

	@LocalServerPort
	private int PORT;

	@Autowired
	private JwtProviderService jwtProviderService;

	private StompSession stompSession;
	private CompletableFuture<String> completableFuture;
	private Long chatRoomId;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private GalleryRepository galleryRepository;

	@Autowired
	private ChatRoomRepository chatRoomRepository;

	@BeforeEach
	void setUp() throws ExecutionException, InterruptedException, TimeoutException {
		String accessToken = jwtProviderService.generateAccessToken(
			1L,
			"test1@example.com",
			"test1",
			"testProfileImage"
		);

		// WebSocket 클라이언트 설정
		WebSocketStompClient webSocketStompClient = new WebSocketStompClient(new StandardWebSocketClient());
		webSocketStompClient.setMessageConverter(new MappingJackson2MessageConverter());

		// WebSocket URL 및 헤더 설정
		String URL = String.format(WEB_SOCKET_URL, PORT);
		WebSocketHttpHeaders webSocketHttpHeaders = new WebSocketHttpHeaders();
		webSocketHttpHeaders.add(ACCESS_TOKEN_HEADER, BEARER + " " + accessToken);

		// WebSocket 연결 및 세션 저장
		stompSession = webSocketStompClient.connect(URL, webSocketHttpHeaders, new StompSessionHandlerAdapter() {
			@Override
			public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
				stompSession = session;
				System.out.println("CONNECTED TO WEBSOCKET");
			}
		}).get(5, TimeUnit.SECONDS);

		Member member = memberRepository.save(MemberFixture.createMemberEntity());
		Gallery gallery = galleryRepository.save(GalleryFixture.createGalleryEntity(member));
		ChatRoom chatRoom = chatRoomRepository.save(ChatFixture.createChatRoomEntity(gallery));

		chatRoomId = chatRoom.getId();

		// CompletableFuture 초기화 및 주제 구독
		completableFuture = new CompletableFuture<>();
		stompSession.subscribe("/sub/ws/" + chatRoomId, new StompFrameHandler() {
			@Override
			public Type getPayloadType(StompHeaders headers) {
				return String.class;
			}

			@Override
			public void handleFrame(StompHeaders headers, Object payload) {
				String receivedMessage = (String)payload;
				System.out.println("MESSAGE RECEIVED: " + receivedMessage);
				completableFuture.complete(receivedMessage);
			}
		});

		// 기존 연결 대기 시간 설정 제거
		Executors.newSingleThreadScheduledExecutor().schedule(() ->
				completableFuture.completeExceptionally(new TimeoutException("Timeout waiting for message")),
			15, TimeUnit.SECONDS
		);
	}
}

