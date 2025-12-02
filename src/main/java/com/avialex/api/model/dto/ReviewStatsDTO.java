package com.avialex.api.model.dto;

import lombok.Builder;

@Builder
public record ReviewStatsDTO(
        double averageRating,
        long totalReviews,
        double satisfactionPercent,
        int nps,
        long totalUsers,
        double fiveStarsPercent
) {}
