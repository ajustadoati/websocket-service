package com.ajustadoati.websocket.application.service;

import com.ajustadoati.websocket.domain.MessageWS;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
@Service
public class WebsocketService extends TextWebSocketHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
    private final Logger log = Logger.getLogger(getClass().getName());

    @Autowired
    private OpenfireService openfireService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {

        TextMessage message = new TextMessage(objectMapper.writeValueAsString("text value"));
        session.sendMessage(message);
        sessions.add(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage inMessage) throws Exception {
        System.out.println("receiving message:" + inMessage.getPayload());
        MessageWS messageWS = objectMapper.readValue(inMessage.getPayload(), MessageWS.class);
        messageWS.setSession(session);
        openfireService.processMessage(messageWS);
    }


    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        log.info("Closing current session");
        //this.connection.disconnect();
        openfireService.removeMessage(session);
        openfireService.removeSession(session);
        session.close();
    }

}
