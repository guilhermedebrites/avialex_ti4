package com.avialex.api.service;

import com.avialex.api.exceptions.ReviewNotFoundException;
import com.avialex.api.model.dto.ReviewRequestDTO;
import com.avialex.api.model.dto.ReviewResponseDTO;
import com.avialex.api.model.entity.Review;
import com.avialex.api.model.entity.User;
import com.avialex.api.repository.ReviewRepository;
import com.avialex.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.avialex.api.model.dto.ReviewStatsDTO;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    @Autowired
    public ReviewService(ReviewRepository reviewRepository, UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
    }

    public ReviewResponseDTO create(ReviewRequestDTO reviewRequestDTO) {
        User user = userRepository.getReferenceById(reviewRequestDTO.userId());
        Review review = Review.builder()
                .user(user)
                .comment(reviewRequestDTO.comment())
                .rating(reviewRequestDTO.rating())
                .reviewType(reviewRequestDTO.reviewType())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        reviewRepository.save(review);
        return getReviewResponseDTO(review);
    }

    public ReviewResponseDTO update(ReviewRequestDTO reviewRequestDTO) {
        Review review = reviewRepository.getReferenceById(reviewRequestDTO.id());

        if (reviewRequestDTO.comment() != null) {
            review.setComment(reviewRequestDTO.comment());
        }
        if (reviewRequestDTO.reviewType() != null) {
            review.setReviewType(reviewRequestDTO.reviewType());
        }
        if (reviewRequestDTO.rating() != null) {
            review.setRating(reviewRequestDTO.rating());
        }
        review.setUpdatedAt(Instant.now());
        reviewRepository.save(review);
        return getReviewResponseDTO(review);
    }

    public ReviewResponseDTO delete(ReviewRequestDTO reviewRequestDTO) {
        Review review = reviewRepository.getReferenceById(reviewRequestDTO.id());
        // Capturar dados ANTES de deletar
        ReviewResponseDTO response = getReviewResponseDTO(review);
        reviewRepository.delete(review);
        return response;
    }

    public Page<ReviewResponseDTO> getAll(Pageable pageable) {
        return reviewRepository.findAll(pageable).map(r ->
                new ReviewResponseDTO(r.getId(), r.getUser().getId(), r.getRating(),
                        r.getComment(), r.getReviewType(),
                        r.getCreatedAt(), r.getUpdatedAt(), r.getUser().getName()));
    }

    public List<ReviewResponseDTO> getByUserId(Integer id) {
        return reviewRepository.findAllByUser_Id(id).stream().map(r ->
                new ReviewResponseDTO(
                        r.getId(),
                        r.getUser().getId(),
                        r.getRating(),
                        r.getComment(),
                        r.getReviewType(),
                        r.getCreatedAt(),
                        r.getUpdatedAt(),
                        r.getUser().getName()
                )
        ).toList();
    }

    public ReviewResponseDTO getById(Integer id) throws ReviewNotFoundException {
        Optional<Review> review = reviewRepository.findById(Long.valueOf(id));
        if (review.isPresent()) {
            return getReviewResponseDTO(review.get());
        } else {
            throw new ReviewNotFoundException(Long.valueOf(id));
        }
    }

    private static ReviewResponseDTO getReviewResponseDTO(Review review) {
        return new ReviewResponseDTO(
                review.getId(),
                review.getUser().getId(),
                review.getRating(),
                review.getComment(),
                review.getReviewType(),
                review.getCreatedAt(),
                review.getUpdatedAt(),
                review.getUser().getName()
        );
    }
    public ReviewStatsDTO getStatistics() {
    List<Review> reviews = reviewRepository.findAll();

    long totalReviews = reviews.size();
    double averageRating = reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);

    long totalUsers = reviews.stream().map(r -> r.getUser().getId()).distinct().count();

    long satisfied = reviews.stream().filter(r -> r.getRating() >= 4).count();
    double satisfactionPercent = totalReviews == 0 ? 0 : (satisfied * 100.0) / totalReviews;

    long promoters = reviews.stream().filter(r -> r.getRating() >= 9).count(); // Ajuste se usar escala 1-5
    long detractors = reviews.stream().filter(r -> r.getRating() <= 6).count();
    int nps = totalReviews == 0 ? 0 :
            (int) (((promoters - detractors) * 100.0) / totalReviews);

    long fiveStars = reviews.stream().filter(r -> r.getRating() == 5).count();
    double fiveStarsPercent = totalReviews == 0 ? 0 : (fiveStars * 100.0) / totalReviews;

    return ReviewStatsDTO.builder()
            .averageRating(averageRating)
            .totalReviews(totalReviews)
            .satisfactionPercent(satisfactionPercent)
            .nps(nps)
            .totalUsers(totalUsers)
            .fiveStarsPercent(fiveStarsPercent)
            .build();
}

}
