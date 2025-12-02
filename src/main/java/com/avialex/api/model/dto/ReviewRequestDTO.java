package com.avialex.api.model.dto;

import com.avialex.api.model.entity.ReviewType;

public record ReviewRequestDTO(Long id, Long userId, Integer rating, String comment, ReviewType reviewType) {
}
