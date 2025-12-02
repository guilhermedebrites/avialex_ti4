package com.avialex.api.controller;

import com.avialex.api.exceptions.ReviewNotFoundException;
import com.avialex.api.model.dto.ReviewRequestDTO;
import com.avialex.api.model.dto.ReviewResponseDTO;
import com.avialex.api.model.dto.ReviewStatsDTO;
import com.avialex.api.model.entity.Review;
import com.avialex.api.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/review")
public class ReviewController {

    private final ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PostMapping
    public ResponseEntity<ReviewResponseDTO> create(@RequestBody ReviewRequestDTO reviewRequestDTO) {
        return ResponseEntity.ok(reviewService.create(reviewRequestDTO));
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PutMapping
    public ResponseEntity<ReviewResponseDTO> update(@RequestBody ReviewRequestDTO reviewRequestDTO) {
        return ResponseEntity.ok(reviewService.update(reviewRequestDTO));
    }

    @PreAuthorize("hasRole('STAFF')")
    @DeleteMapping
    public ResponseEntity<ReviewResponseDTO> delete(@RequestBody ReviewRequestDTO reviewRequestDTO) {
        return ResponseEntity.ok(reviewService.delete(reviewRequestDTO));
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<Page<ReviewResponseDTO>> getAll(@PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(reviewService.getAll(pageable));
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReviewResponseDTO>> getByUser(@PathVariable Integer userId) {
        return ResponseEntity.ok(reviewService.getByUserId(userId));
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<ReviewResponseDTO> getById(@PathVariable Integer id) throws ReviewNotFoundException {
        return ResponseEntity.ok(reviewService.getById(id));
    }
   @PreAuthorize("hasAnyRole('USER','ADMIN')")
   @GetMapping("/stats")
    public ResponseEntity<ReviewStatsDTO> getStats() {
    return ResponseEntity.ok(reviewService.getStatistics());
}


}
