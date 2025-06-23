package com.ajustadoati.websocket.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Data
@ConfigurationProperties("openfire")
public class OpenfireProperties {

    private String domain;
    private String port;
    private String user;
    private String password;

}


