package com.avialex.api.model.dto;

public record MonthlyProcessStats(
        String month,
        Long wonProcesses,
        Long lostProcesses
) {}