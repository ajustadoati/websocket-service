package com.ajustadoati.websocket.config;

import com.ajustadoati.websocket.config.properties.OpenfireProperties;
import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.springframework.stereotype.Component;

@Component
public class XmppConnectionManager {

    private final OpenfireProperties properties;
    private AbstractXMPPConnection connection;
    private ChatManager chatManager;

    public XmppConnectionManager(OpenfireProperties properties) {
        this.properties = properties;
        initializeConnection();
    }

    private void initializeConnection() {
        try {
            var config = XMPPTCPConnectionConfiguration.builder()
                .setUsernameAndPassword(properties.getUser(), properties.getPassword())
                .setHost(properties.getDomain())
                .setXmppDomain(properties.getDomain())
                .setResource("Microservice client")
                .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                .setPort(Integer.parseInt(properties.getPort()))
                .build();

            SASLAuthentication.unBlacklistSASLMechanism("PLAIN");
            SASLAuthentication.blacklistSASLMechanism("SCRAM-SHA-1");

            connection = new XMPPTCPConnection(config);
            connection.connect().login();
            chatManager = ChatManager.getInstanceFor(connection);

        } catch (Exception e) {
            throw new IllegalStateException("Could not initialize XMPP connection", e);
        }
    }

    public ChatManager getChatManager() {
        return chatManager;
    }
}

