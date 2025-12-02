package com.avialex.api.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProcessHistoryDTO(
        Long id,
        Integer processNumber,
        String name,
        Long clientId,
        String clientName,
        String status,
        LocalDateTime creationDate,
        LocalDateTime lastModifiedDate,
        BigDecimal recoveredValue,
        Boolean won
) {}

