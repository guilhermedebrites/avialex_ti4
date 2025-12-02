package com.avialex.api.model.dto;

import java.math.BigDecimal;
import java.util.List;

public record MainDashboardResponseDTO(
        Long activeProcess,
        Long activeClients,
        BigDecimal recoveredValue,
        Long SuccessFee,
        List<MonthlyProcessStats> monthlyStats,
        Long totalProcesses
) {}