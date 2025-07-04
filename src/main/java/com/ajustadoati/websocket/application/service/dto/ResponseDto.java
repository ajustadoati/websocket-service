package com.ajustadoati.websocket.application.service.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ResponseDto {
    private String user;
    private String message;
    private BigDecimal latitude;
    private BigDecimal longitude;
}
