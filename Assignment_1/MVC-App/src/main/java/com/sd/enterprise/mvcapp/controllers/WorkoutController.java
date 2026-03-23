package com.sd.enterprise.mvcapp.controllers;

import com.sd.enterprise.mvcapp.dto.PageResponse;
import com.sd.enterprise.mvcapp.dto.WorkoutRequest;
import com.sd.enterprise.mvcapp.dto.WorkoutResponse;
import com.sd.enterprise.mvcapp.entity.Difficulty;
import com.sd.enterprise.mvcapp.entity.MuscleGroup;
import com.sd.enterprise.mvcapp.services.WorkoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/workouts")
@RequiredArgsConstructor
public class WorkoutController {

    private final WorkoutService workoutService;

    // GET /api/workouts?name=&muscleGroup=&difficulty=&page=0&size=10&sortBy=name&sortDir=asc
    @GetMapping
    public ResponseEntity<PageResponse<WorkoutResponse>> getWorkouts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) MuscleGroup muscleGroup,
            @RequestParam(required = false) Difficulty difficulty,
            @RequestParam(required = false) Integer minDuration,
            @RequestParam(required = false) Integer maxDuration,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        return ResponseEntity.ok(workoutService.getWorkouts(
                name, muscleGroup, difficulty, minDuration, maxDuration,
                page, size, sortBy, sortDir));
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkoutResponse> getWorkout(@PathVariable Long id) {
        return ResponseEntity.ok(workoutService.getWorkoutById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<WorkoutResponse> createWorkout(@Valid @RequestBody WorkoutRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(workoutService.createWorkout(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<WorkoutResponse> updateWorkout(
            @PathVariable Long id,
            @Valid @RequestBody WorkoutRequest request) {
        return ResponseEntity.ok(workoutService.updateWorkout(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteWorkout(@PathVariable Long id) {
        workoutService.deleteWorkout(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(workoutService.getStats());
    }
}
