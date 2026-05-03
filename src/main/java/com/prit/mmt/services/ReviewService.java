package com.prit.mmt.services;

import com.prit.mmt.models.Review;
import com.prit.mmt.repositories.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ReviewService {
    @Autowired
    private ReviewRepository reviewRepository;

    public Review addReview(Review review) {
        review.setCreatedAt(LocalDateTime.now().toString());
        return reviewRepository.save(review);
    }

    public List<Review> getReviews(String targetId, String targetType) {
        return reviewRepository.findByTargetIdAndTargetType(targetId, targetType);
    }

    public Review addReply(String reviewId, Review.Reply reply) {
        Optional<Review> optionalReview = reviewRepository.findById(reviewId);
        if (optionalReview.isPresent()) {
            Review review = optionalReview.get();
            reply.setId(UUID.randomUUID().toString());
            reply.setCreatedAt(LocalDateTime.now().toString());
            review.getReplies().add(reply);
            return reviewRepository.save(review);
        }
        throw new RuntimeException("Review not found");
    }

    public Review voteHelpful(String reviewId) {
        Optional<Review> optionalReview = reviewRepository.findById(reviewId);
        if (optionalReview.isPresent()) {
            Review review = optionalReview.get();
            review.setHelpfulVotes(review.getHelpfulVotes() + 1);
            return reviewRepository.save(review);
        }
        throw new RuntimeException("Review not found");
    }

    public Review flagReview(String reviewId) {
        Optional<Review> optionalReview = reviewRepository.findById(reviewId);
        if (optionalReview.isPresent()) {
            Review review = optionalReview.get();
            review.setFlags(review.getFlags() + 1);
            return reviewRepository.save(review);
        }
        throw new RuntimeException("Review not found");
    }
}