package com.avialex.api.model.dto;

public record AuthResponseDTO(String accessToken, long expiresIn, String refreshToken) {}


