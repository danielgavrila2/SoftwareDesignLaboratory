package com.fitness.activityservice.service;

import com.fitness.activityservice.ActivityRepository;
import com.fitness.activityservice.dto.ActivityRequest;
import com.fitness.activityservice.model.Activity;
import com.fitness.activityservice.sto.ActivityResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final UserValidationservice userValidationservice;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topic}")
    private String topicName;

    public ActivityResponse trackActivity(ActivityRequest request) {
        Boolean isValidUser = userValidationservice.validateUser(request.getUserId());
        if (!isValidUser) {
            throw new RuntimeException("Invalid user ID: " + request.getUserId());
        }

        Activity activity = Activity.builder()
                .userId(request.getUserId())
                .activityType(request.getActivityType())
                .duration(request.getDuration())
                .caloriesBurned(request.getCaloriesBurned())
                .startTime(request.getStartTime())
                .additionalMetrics(request.getAdditionalMetrics())
                .build();

        Activity savedActivity = activityRepository.save(activity);

        try {; // now works
            kafkaTemplate.send("activity-events", activity);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return mapToResponse(savedActivity);
    }

    private ActivityResponse mapToResponse(Activity activity) {
        ActivityResponse response = new ActivityResponse();
        response.setId(activity.getId());
        response.setUserId(activity.getUserId());
        response.setActivityType(activity.getActivityType());
        response.setDuration(activity.getDuration());
        response.setCaloriesBurned(activity.getCaloriesBurned());
        response.setStartTime(activity.getStartTime());
        response.setAdditionalMetrics(activity.getAdditionalMetrics());
        response.setCreatedAt(activity.getCreatedAt());
        response.setUpdatedAt(activity.getUpdatedAt());
        return response;
    }
    public List<ActivityResponse> getUserActivities(String userId) {
        List<Activity> activities = activityRepository.findByUserId(userId);
        return activities.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ActivityResponse getActivityById(String activityId) {
        return activityRepository.findById(activityId)
                .map(this::mapToResponse)
                .orElseThrow(() -> new RuntimeException("Activity not found with id: " + activityId));
    }

    public void deleteActivity(String activityId, String userId) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new RuntimeException("Activity not found"));
        
        if (!activity.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized to delete this activity");
        }
        
        activityRepository.deleteById(activityId);
        log.info("Deleted activity {} for user {}", activityId, userId);
    }

    public ActivityResponse updateActivity(String activityId, ActivityRequest request, String userId) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new RuntimeException("Activity not found"));
        
        if (!activity.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized to update this activity");
        }
        
        activity.setDuration(request.getDuration());
        activity.setCaloriesBurned(request.getCaloriesBurned());
        if (request.getActivityType() != null) {
            activity.setActivityType(request.getActivityType());
        }
        if (request.getStartTime() != null) {
            activity.setStartTime(request.getStartTime());
        }
        
        Activity updated = activityRepository.save(activity);
        log.info("Updated activity {} for user {}", activityId, userId);
        
        return mapToResponse(updated);
    }
}
