package com.ajustadoati.websocket.application.service;

import com.ajustadoati.websocket.application.service.dto.ResponseDto;
import com.ajustadoati.websocket.config.properties.OpenfireProperties;
import com.ajustadoati.websocket.domain.MessageWS;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
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
@AllArgsConstructor
@Service
public class OpenfireService {
    private static AbstractXMPPConnection connection;
    private final Logger log = Logger.getLogger(getClass().getName());
    private ChatManager chatManager;
    private OpenfireProperties openfireProperties;
    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
    private static List<MessageWS> messages= new ArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public OpenfireService(OpenfireProperties openfireProperties) {
        log.info("init connection admin in opnefire");
        this.openfireProperties = openfireProperties;
        var configBuilder = XMPPTCPConnectionConfiguration.builder();
        configBuilder.setUsernameAndPassword(openfireProperties.getUser(), openfireProperties.getPassword());
        try {
            configBuilder.setResource("Microservice client");
            configBuilder.setHost(openfireProperties.getDomain());
            configBuilder.setXmppDomain(openfireProperties.getDomain());
            configBuilder.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
            configBuilder.setPort(Integer.parseInt(openfireProperties.getPort()));
            SASLAuthentication.unBlacklistSASLMechanism("PLAIN");
            SASLAuthentication.blacklistSASLMechanism("SCRAM-SHA-1");
        } catch (XmppStringprepException e) {
            e.printStackTrace();
        }
        connection = new XMPPTCPConnection(configBuilder.build());
        // Connect to the server
        try {
            if (!connection.isConnected()) {
                connection.connect();
                connection.login();
                chatManager = org.jivesoftware.smack.chat2.ChatManager.getInstanceFor(connection);
                chatManager.addIncomingListener(getIncoming());
            }
        } catch (SmackException | IOException | XMPPException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void processMessage(MessageWS messageWS) throws Exception{
        messages.add(messageWS);
        sessions.add(messageWS.getSession());
        var users = messageWS.getUsers().split("&&");
        for(String user : users){
            log.info(">%s<".formatted(user));
            //List<String> listDevicesForUser=Util.getDevicesForUser(dispositivos, user);
            user=user+"@ajustadoati.com";
            log.info("User: %s".formatted(user));
            var jid = JidCreate.entityBareFrom(user);
            var chat = getChatManager().chatWith(jid);
            var outMessage= new Message();
            outMessage.setBody(messageWS.getId()+"---"+messageWS.getMessage()+"---"+messageWS.getLatitude()+"---"+messageWS.getLongitude());
            try {
                chat.send(outMessage);
                /*if(listDevicesForUser!=null && listDevicesForUser.size()>0){
                    Util.sendNotifications(listDevicesForUser, "Hola, Han realizado una solicitud: "+msj.getMensaje()+", con Id:"+msj.getId());
                }*/
            } catch (SmackException.NotConnectedException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public MessageWS getMessageBySession(WebSocketSession session){
        log.info("Searching message");
        for (MessageWS messageWS : messages) {
            if(messageWS.getSession().equals(session)){
                log.info("session"+messageWS.getSession());
                return messageWS;
            }
        }
        return null;
    }

    public void removeMessage(WebSocketSession session){
        messages.remove(getMessageBySession(session));
    }

    public void removeSession(WebSocketSession session){
        sessions.remove(session);
    }

    private WebSocketSession getSessionById(Long id){
        log.info("searching session%d".formatted(id));
        for (MessageWS messageWS : messages) {
            if(messageWS.getId().equals(id)){
                log.info("session"+messageWS.getSession());
                return messageWS.getSession();
            }
        }
        return null;
    }

    private IncomingChatMessageListener getIncoming(){
        return (from, message, chat) -> {
            log.info("New message from " + from + ": " + message.getBody());
            log.info("Received message from proveedor: " + message.getBody());
            String[] codearr = message.getBody()
                .split("&&");
            Long id = 0L;
            String texto = "";
            if (codearr.length > 1) {
                id = Long.valueOf(codearr[0]);
                texto = codearr[1];
            }

            String[] arr = from.toString()
                .split("@");
            String us = arr[0];
            var responseDto = ResponseDto.builder()
                .message(texto)
                .latitude(0.0)
                .longitude(0.0)
                .user(us)
                .build();

            try {
                var response = objectMapper.writeValueAsString(responseDto);
                synchronized (getSessions()) {
                    for (WebSocketSession session : getSessions()) {
                        if (session.equals(getSessionById(id))) {
                            log.info("Sending response");
                            session.sendMessage(new TextMessage(response));
                        }
                    }
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        };
    }

}
