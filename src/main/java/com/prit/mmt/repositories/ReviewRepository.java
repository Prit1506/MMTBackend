package com.prit.mmt.repositories;

import com.prit.mmt.models.Review;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ReviewRepository extends MongoRepository<Review, String> {
    List<Review> findByTargetIdAndTargetType(String targetId, String targetType);
}