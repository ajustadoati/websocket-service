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
import org.jivesoftware.smack.packet.Message;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Data
@RequiredArgsConstructor
@Service
@Slf4j
public class OpenfireService {
    private final XmppConnectionManager connectionManager;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
    private final List<MessageWS> messages = new CopyOnWriteArrayList<>();
    private final ProviderService providerService;

    @PostConstruct
    public void init() {
        connectionManager.getChatManager()
            .addIncomingListener(this::handleIncomingMessage);
    }

    public void processMessage(MessageWS messageWS) {
        messages.add(messageWS);
        sessions.add(messageWS.getSession());
        messageWS.getUsers()
            .stream()
            .map(String::trim)
            .filter(user -> !user.isEmpty())
            .forEach(user -> {
                try {
                    try {
                        sendMessageToUser(messageWS, user);
                    } catch (SmackException.NotConnectedException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                } catch (XmppStringprepException e) {
                    log.error("Error processing user {}", user, e);
                }
            });
    }

    private void sendMessageToUser(MessageWS messageWS, String user)
        throws XmppStringprepException, SmackException.NotConnectedException, InterruptedException {
        log.info("Sending message to user: {} {}", user, messageWS);
        EntityBareJid jid = JidCreate.entityBareFrom(user + "@ajustadoati.com");
        Chat chat = connectionManager.getChatManager()
            .chatWith(jid);
        String gmapsUrl = String.format("https://www.google.com/maps?q=%s,%s",
            messageWS.getLatitude(), messageWS.getLongitude());

        String outBody = String.join("\n",
            "Id:" + messageWS.getId(),
            "Sol:" + messageWS.getMessage(),
            "Loc:" + gmapsUrl);
        var outMessage = new Message();
        outMessage.setBody(outBody);
        chat.send(outMessage);
    }

    private void handleIncomingMessage(EntityBareJid from, Message message, Chat chat) {
        log.info("Received message from {}: {}", from, message.getBody());

        String[] parts = message.getBody().split("::");
        if (parts.length < 2) return;

        Long id = Long.parseLong(parts[0]);
        String content = parts[1];

        String username = from.toString().split("@")[0];

        ResponseDto response = ResponseDto.builder()
            .user(username)
            .message(content)
            .build();

        var provider = providerService.findByUsername(username)
            .block();

        if (provider != null) {
            response.setLatitude(provider.getLocations().get(0).getLatitude());
            response.setLongitude(provider.getLocations().get(0).getLongitude());
        }else{
            response.setLatitude(BigDecimal.ZERO);
            response.setLongitude(BigDecimal.ZERO);
        }

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
        messages.removeIf(m -> m.getSession()
            .equals(session));
    }

    private WebSocketSession getSessionById(Long id) {
        return messages.stream()
            .filter(m -> m.getId()
                .equals(id))
            .map(MessageWS::getSession)
            .findFirst()
            .orElse(null);
    }

}
