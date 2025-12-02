package com.avialex.api.model.dto;

import com.avialex.api.model.entity.UserType;

public record SignUpRequestDTO(
        String name,
        String address,
        String email,
        String phone,
        String password,
        String cpf,
        String rg,
        UserType type
) {}