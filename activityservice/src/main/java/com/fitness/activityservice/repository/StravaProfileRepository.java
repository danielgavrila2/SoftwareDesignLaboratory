package com.fitness.activityservice.repository;

import com.fitness.activityservice.model.StravaProfile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StravaProfileRepository extends MongoRepository<StravaProfile, String> {
    Optional<StravaProfile> findByUserId(String userId);
}
