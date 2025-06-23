package com.ajustadoati.websocket.config;

import com.ajustadoati.websocket.application.service.WebsocketService;
import com.ajustadoati.websocket.config.properties.OpenfireProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(getWebsocketService(), "openfire").setAllowedOrigins("*");
    }

    @Bean
    public WebsocketService getWebsocketService(){
        return new WebsocketService();
    }

}
