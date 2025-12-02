package com.avialex.api.model.dto;

public record ResetPasswordRequestDTO(String token, String newPassword) {}


