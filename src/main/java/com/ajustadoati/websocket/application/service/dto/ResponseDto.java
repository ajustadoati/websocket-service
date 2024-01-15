package com.ajustadoati.websocket.application.service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResponseDto {
    private String user;
    private String message;
    private Double latitude;
    private Double longitude;
}
