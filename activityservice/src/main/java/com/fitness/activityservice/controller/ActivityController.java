package com.fitness.activityservice.controller;

import com.fitness.activityservice.dto.ActivityRequest;
import com.fitness.activityservice.service.ActivityService;
import com.fitness.activityservice.service.StravaIntegrationService;
import com.fitness.activityservice.sto.ActivityResponse;
import com.fitness.activityservice.pattern.template.CsvExport;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activities")
public class ActivityController {
    private final ActivityService activityService;
    private final StravaIntegrationService stravaIntegrationService;

    public ActivityController(ActivityService activityService, StravaIntegrationService stravaIntegrationService) {
        this.activityService = activityService;
        this.stravaIntegrationService = stravaIntegrationService;
    }

    @PostMapping
    public ResponseEntity<ActivityResponse> trackActivity(@RequestBody ActivityRequest request , @RequestHeader("X-User-ID") String userId) {
        request.setUserId(userId);
        return ResponseEntity.ok(activityService.trackActivity(request));
    }
    
    @PostMapping("/strava/config")
    public ResponseEntity<com.fitness.activityservice.model.StravaProfile> saveStravaConfig(@RequestHeader("X-User-ID") String userId, @RequestParam String clientId, @RequestParam String clientSecret) {
        return ResponseEntity.ok(stravaIntegrationService.saveConfig(userId, clientId, clientSecret));
    }

    @GetMapping("/strava/config")
    public ResponseEntity<com.fitness.activityservice.model.StravaProfile> getStravaConfig(@RequestHeader("X-User-ID") String userId) {
        return stravaIntegrationService.getConfig(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/strava/sync")
    public ResponseEntity<String> syncStrava(@RequestHeader("X-User-ID") String userId) {
        List<ActivityRequest> stravaActivities = stravaIntegrationService.fetchStravaActivitiesForUser(userId);
        for(ActivityRequest req : stravaActivities) {
            req.setUserId(userId);
            activityService.trackActivity(req);
        }
        return ResponseEntity.ok("Successfully synced " + stravaActivities.size() + " activities from Strava.");
    }

    @PostMapping("/strava/exchange")
    public ResponseEntity<String> exchangeStravaToken(@RequestHeader("X-User-ID") String userId, @RequestParam String code) {
        List<ActivityRequest> stravaActivities = stravaIntegrationService.exchangeTokenAndFetch(userId, code);
        for(ActivityRequest req : stravaActivities) {
            req.setUserId(userId);
            activityService.trackActivity(req);
        }
        return ResponseEntity.ok("Successfully authenticated and synced " + stravaActivities.size() + " activities from Strava.");
    }

    @GetMapping
    public ResponseEntity<List<ActivityResponse>> getUserActivities(@RequestHeader("X-User-ID") String userId){
        return ResponseEntity.ok(activityService.getUserActivities(userId));
    }

    @GetMapping("/{activityId}")
    public ResponseEntity<ActivityResponse> getActivity(@PathVariable String activityId){
        return ResponseEntity.ok(activityService.getActivityById(activityId));
    }

    @DeleteMapping("/{activityId}")
    public ResponseEntity<Void> deleteActivity(@PathVariable String activityId, @RequestHeader("X-User-ID") String userId) {
        activityService.deleteActivity(activityId, userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{activityId}")
    public ResponseEntity<ActivityResponse> updateActivity(@PathVariable String activityId, @RequestBody ActivityRequest request, @RequestHeader("X-User-ID") String userId) {
        return ResponseEntity.ok(activityService.updateActivity(activityId, request, userId));
    }

    @GetMapping("/export/csv")
    public ResponseEntity<String> exportUserActivitiesCsv(@RequestHeader("X-User-ID") String userId) {
        List<ActivityResponse> activities = activityService.getUserActivities(userId);
        CsvExport exporter = new CsvExport();
        String csvData = exporter.export(activities);
        
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=activities.csv");
        headers.add("Content-Type", "text/csv");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(csvData);
    }
}
