package com.ajustadoati.websocket.adapter.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController(value="/api")
public class WebsocketController {

    @GetMapping("info")
    public String getInfo(){
        return "Websocket Controller Info";
    }
}
