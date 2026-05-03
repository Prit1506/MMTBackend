package com.prit.mmt.controllers;

import com.prit.mmt.models.Review;
import com.prit.mmt.services.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@CrossOrigin(origins = "*")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @PostMapping
    public Review addReview(@RequestBody Review review) {
        return reviewService.addReview(review);
    }

    @GetMapping
    public List<Review> getReviews(@RequestParam String targetId, @RequestParam String targetType) {
        return reviewService.getReviews(targetId, targetType);
    }

    @PostMapping("/{reviewId}/reply")
    public Review addReply(@PathVariable String reviewId, @RequestBody Review.Reply reply) {
        return reviewService.addReply(reviewId, reply);
    }

    @PostMapping("/{reviewId}/helpful")
    public Review voteHelpful(@PathVariable String reviewId) {
        return reviewService.voteHelpful(reviewId);
    }

    @PostMapping("/{reviewId}/flag")
    public Review flagReview(@PathVariable String reviewId) {
        return reviewService.flagReview(reviewId);
    }
}