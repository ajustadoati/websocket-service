package com.ajustadoati.websocket.application.service.dto;

import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Data
public class ProviderDto {
    private Integer userId;
    private String name;
    private String email;
    private String username;
    private String mobileNumber;
    private Instant createdAt;
    private Set<String> roles;
    private Set<String> categories;
    private List<LocationDto> locations;
}
