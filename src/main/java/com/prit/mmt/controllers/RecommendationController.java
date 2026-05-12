package com.prit.mmt.controllers;

import com.prit.mmt.models.RecommendationResponse;
import com.prit.mmt.models.Users;
import com.prit.mmt.services.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/recommendations")
@CrossOrigin(origins = "*")
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;

    /**
     * GET /recommendations?userId=xxx
     * Returns a personalized list of recommended flights and hotels.
     * userId is optional — anonymous users get trending results.
     */
    @GetMapping
    public ResponseEntity<List<RecommendationResponse>> getRecommendations(
            @RequestParam(required = false) String userId) {
        List<RecommendationResponse> recs = recommendationService.getRecommendations(userId);
        return ResponseEntity.ok(recs);
    }

    /**
     * POST /recommendations/feedback?userId=xxx&targetId=xxx&targetType=FLIGHT&helpful=true
     * Records user feedback (helpful / not helpful) to refine future suggestions.
     */
    @PostMapping("/feedback")
    public ResponseEntity<Users> submitFeedback(
            @RequestParam String userId,
            @RequestParam String targetId,
            @RequestParam String targetType,
            @RequestParam boolean helpful) {
        Users updatedUser = recommendationService.submitFeedback(userId, targetId, targetType, helpful);
        return ResponseEntity.ok(updatedUser);
    }
}