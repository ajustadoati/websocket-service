package com.ajustadoati.websocket.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebsocketServiceTest {

    @Mock
    private OpenfireService openfireService;

    @Mock
    private WebSocketSession webSocketSession;

    @InjectMocks
    private WebsocketService websocketService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAfterConnectionEstablished() throws Exception {
        websocketService.afterConnectionEstablished(webSocketSession);
        verify(webSocketSession, times(1)).sendMessage(any(TextMessage.class));
    }

    @Test
    void testHandleTextMessage() throws Exception {
        TextMessage message = new TextMessage("{\"id\":7,\"message\":\"test from websocket\",\"users\":\"ricroj&&elba\",\"latitude\":1.3,\"longitude\":1.5}");
        websocketService.handleTextMessage(webSocketSession, message);
        verify(openfireService, times(1)).processMessage(any()); // Verify that processMessage method is called in OpenfireService
    }

    @Test
    void testAfterConnectionClosed() throws Exception {
        CloseStatus closeStatus = new CloseStatus(1000, "Normal closure");
        websocketService.afterConnectionClosed(webSocketSession, closeStatus);
        verify(openfireService, times(1)).removeSession(any()); // Verify that removeMessage method is called in OpenfireService

    }

}
