package com.ajustadoati.websocket.application.service;

import com.ajustadoati.websocket.application.service.dto.ResponseDto;
import com.ajustadoati.websocket.config.XmppConnectionManager;
import com.ajustadoati.websocket.domain.MessageWS;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

@Data
@RequiredArgsConstructor
@Service
@Slf4j
public class OpenfireService {
    private final XmppConnectionManager connectionManager;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
    private final List<MessageWS> messages = new CopyOnWriteArrayList<>();

    @PostConstruct
    public void init() {
        connectionManager.getChatManager().addIncomingListener(this::handleIncomingMessage);
    }

    public void processMessage(MessageWS messageWS) {
        messages.add(messageWS);
        sessions.add(messageWS.getSession());

        var users = messageWS.getUsers().split("&&");

        for (String user : users) {
            try {
                var fullJid = JidCreate.entityBareFrom(user + "@ajustadoati.com");
                var chat = connectionManager.getChatManager().chatWith(fullJid);
                var outMessage = new Message();
                outMessage.setBody(String.join("---",
                    messageWS.getId().toString(),
                    messageWS.getMessage(),
                    String.valueOf(messageWS.getLatitude()),
                    String.valueOf(messageWS.getLongitude())));
                chat.send(outMessage);

            } catch (Exception e) {
                log.error("Error sending message to user {}", user, e);
            }
        }
    }

    private void handleIncomingMessage(EntityBareJid from, Message message, Chat chat) {
        log.info("Received message from {}: {}", from, message.getBody());

        String[] parts = message.getBody().split("&&");
        if (parts.length < 2) return;

        Long id = Long.parseLong(parts[0]);
        String content = parts[1];

        String username = from.toString().split("@")[0];

        ResponseDto response = ResponseDto.builder()
            .user(username)
            .message(content)
            .latitude(0.0)
            .longitude(0.0)
            .build();

        try {
            String json = objectMapper.writeValueAsString(response);
            WebSocketSession targetSession = getSessionById(id);
            if (targetSession != null && targetSession.isOpen()) {
                targetSession.sendMessage(new TextMessage(json));
            }
        } catch (IOException e) {
            log.error("Error sending WebSocket message", e);
        }
    }

    public void removeSession(WebSocketSession session) {
        sessions.remove(session);
        messages.removeIf(m -> m.getSession().equals(session));
    }

    private WebSocketSession getSessionById(Long id) {
        return messages.stream()
            .filter(m -> m.getId().equals(id))
            .map(MessageWS::getSession)
            .findFirst()
            .orElse(null);
    }

}
