package com.ajustadoati.websocket.application.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
public class LocationDto {
    private BigDecimal latitude;
    private BigDecimal longitude;
}

