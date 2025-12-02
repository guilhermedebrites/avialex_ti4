package com.avialex.api.model.dto;

import com.avialex.api.model.enums.ProcessStatus;
import java.math.BigDecimal;

public record UpdateProcessStatusRequest(
        ProcessStatus status,
        BigDecimal recoveredValue,
        Boolean won
) {}

