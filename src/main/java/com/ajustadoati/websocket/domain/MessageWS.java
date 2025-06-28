package com.ajustadoati.websocket.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.socket.WebSocketSession;
import org.jivesoftware.smack.chat2.ChatManager;

import java.util.List;

@Data
@NoArgsConstructor
public class MessageWS {

    private Long id;
    private List<String> users;
    private String message;
    private String latitude;
    private String longitude;
    @JsonIgnore
    private String code;
    @JsonIgnore
    private WebSocketSession session;
    @JsonIgnore
    private ChatManager chatManager;

}
