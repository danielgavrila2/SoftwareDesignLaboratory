package com.fitness.activityservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.activityservice.dto.ActivityRequest;
import com.fitness.activityservice.model.ActivityType;
import com.fitness.activityservice.model.StravaProfile;
import com.fitness.activityservice.repository.StravaProfileRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class StravaIntegrationService {
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final StravaProfileRepository profileRepository;

    public StravaIntegrationService(StravaProfileRepository profileRepository) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        this.profileRepository = profileRepository;
    }

    public StravaProfile saveConfig(String userId, String clientId, String clientSecret) {
        StravaProfile profile = profileRepository.findByUserId(userId).orElse(new StravaProfile());
        profile.setUserId(userId);
        profile.setClientId(clientId);
        profile.setClientSecret(clientSecret);
        return profileRepository.save(profile);
    }

    public Optional<StravaProfile> getConfig(String userId) {
        return profileRepository.findByUserId(userId);
    }

    public List<ActivityRequest> exchangeTokenAndFetch(String userId, String code) {
        StravaProfile profile = profileRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Strava configuration not found for user."));

        try {
            String tokenUrl = "https://www.strava.com/oauth/token";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("client_id", profile.getClientId());
            map.add("client_secret", profile.getClientSecret());
            map.add("code", code);
            map.add("grant_type", "authorization_code");

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(tokenUrl, request, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            
            profile.setAccessToken(root.path("access_token").asText());
            profile.setRefreshToken(root.path("refresh_token").asText());
            profile.setExpiresAt(root.path("expires_at").asLong());
            profileRepository.save(profile);
            
            log.info("Successfully exchanged authorization code for access token.");
            
            return fetchStravaActivities(profile);
            
        } catch (Exception e) {
            log.error("Failed to exchange Strava token: {}", e.getMessage());
            throw new RuntimeException("Failed to authenticate with Strava", e);
        }
    }

    public List<ActivityRequest> fetchStravaActivitiesForUser(String userId) {
        StravaProfile profile = profileRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Strava configuration not found for user."));
        return fetchStravaActivities(profile);
    }

    private List<ActivityRequest> fetchStravaActivities(StravaProfile profile) {
        // Refresh token if expired
        if (profile.getExpiresAt() != null && Instant.now().getEpochSecond() > profile.getExpiresAt()) {
            log.info("Strava access token expired. Refreshing...");
            refreshAccessToken(profile);
        }

        List<ActivityRequest> activities = new ArrayList<>();
        
        try {
            String url = "https://www.strava.com/api/v3/athlete/activities";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(profile.getAccessToken());
            HttpEntity<String> entity = new HttpEntity<>("parameters", headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            JsonNode rootArray = objectMapper.readTree(response.getBody());
            
            for (JsonNode node : rootArray) {
                ActivityRequest request = new ActivityRequest();
                
                String type = node.path("type").asText();
                request.setActivityType(mapStravaType(type));
                
                int durationSeconds = node.path("elapsed_time").asInt(0);
                request.setDuration(Math.max(1, durationSeconds / 60));
                
                double kilojoules = node.path("kilojoules").asDouble(0.0);
                int calories = kilojoules > 0 ? (int) (kilojoules * 0.239) : (request.getDuration() * 10);
                request.setCaloriesBurned(calories);
                
                Map<String, Object> metrics = new HashMap<>();
                
                // Add distance in km
                double distanceMeters = node.path("distance").asDouble(0.0);
                if (distanceMeters > 0) {
                    metrics.put("distance_km", String.format("%.2f", distanceMeters / 1000.0));
                }

                // Add average speed km/h
                double speedMs = node.path("average_speed").asDouble(0.0);
                if (speedMs > 0) {
                    metrics.put("average_speed_kmh", String.format("%.2f", speedMs * 3.6));
                }

                // Add LatLng
                JsonNode startLatLng = node.path("start_latlng");
                if (startLatLng.isArray() && startLatLng.size() == 2) {
                    metrics.put("start_lat", startLatLng.get(0).asDouble());
                    metrics.put("start_lng", startLatLng.get(1).asDouble());
                }

                request.setAdditionalMetrics(metrics);
                activities.add(request);
            }
            log.info("Successfully fetched {} activities from Strava", activities.size());
            
        } catch (Exception e) {
            log.error("Failed to fetch activities from Strava: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch activities from Strava. Make sure your token has 'activity:read_all' scope!", e);
        }
        
        return activities;
    }

    private void refreshAccessToken(StravaProfile profile) {
        try {
            String tokenUrl = "https://www.strava.com/oauth/token";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("client_id", profile.getClientId());
            map.add("client_secret", profile.getClientSecret());
            map.add("grant_type", "refresh_token");
            map.add("refresh_token", profile.getRefreshToken());

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(tokenUrl, request, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            
            profile.setAccessToken(root.path("access_token").asText());
            profile.setRefreshToken(root.path("refresh_token").asText());
            profile.setExpiresAt(root.path("expires_at").asLong());
            profileRepository.save(profile);
            log.info("Strava access token successfully refreshed");
        } catch (Exception e) {
            log.error("Failed to refresh Strava token", e);
            throw new RuntimeException("Failed to refresh Strava token", e);
        }
    }
    
    private ActivityType mapStravaType(String stravaType) {
        if (stravaType == null) return ActivityType.RUNNING;
        switch (stravaType) {
            case "Run": return ActivityType.RUNNING;
            case "Ride": return ActivityType.CYCLING;
            case "Swim": return ActivityType.SWIMMING;
            case "Walk": return ActivityType.WALKING;
            case "Yoga": return ActivityType.YOGA;
            case "WeightTraining": return ActivityType.STRENGTH_TRAINING;
            case "Workout": return ActivityType.HIIT;
            case "Rowing": return ActivityType.ROWING;
            default: return ActivityType.RUNNING; // Default fallback
        }
    }
}
