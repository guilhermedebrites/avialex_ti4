package com.avialex.api.model.dto;

import com.avialex.api.model.entity.ReviewType;

import java.time.Instant;

public record ReviewResponseDTO(Long id, Long userId, Integer rating, String comment, ReviewType reviewType, Instant reviewDate, Instant lastModified, String username) {
}
