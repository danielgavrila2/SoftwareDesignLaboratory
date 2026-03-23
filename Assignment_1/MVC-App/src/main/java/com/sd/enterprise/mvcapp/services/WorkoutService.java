package com.sd.enterprise.mvcapp.services;

import com.sd.enterprise.mvcapp.dto.PageResponse;
import com.sd.enterprise.mvcapp.dto.WorkoutRequest;
import com.sd.enterprise.mvcapp.dto.WorkoutResponse;
import com.sd.enterprise.mvcapp.entity.Difficulty;
import com.sd.enterprise.mvcapp.entity.MuscleGroup;
import com.sd.enterprise.mvcapp.entity.User;
import com.sd.enterprise.mvcapp.entity.Workout;
import com.sd.enterprise.mvcapp.exception.ResourceNotFoundException;
import com.sd.enterprise.mvcapp.repository.UserRepository;
import com.sd.enterprise.mvcapp.repository.WorkoutRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WorkoutService {

    private final WorkoutRepository workoutRepository;
    private final UserRepository userRepository;

    // Read
    @Transactional(readOnly = true)
    public PageResponse<WorkoutResponse> getWorkouts(String name, MuscleGroup muscleGroup, Difficulty difficulty, Integer minDuration, Integer maxDuration, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Workout> workoutPage = workoutRepository.findWithFilters(name, muscleGroup, difficulty, minDuration, maxDuration, pageable);

        Page<WorkoutResponse> responsePage = workoutPage.map(WorkoutResponse::fromEntity);

        return PageResponse.from(responsePage);
    }

    @Transactional(readOnly = true)
    public WorkoutResponse getWorkoutById(Long id) {
        Workout workout = workoutRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workout not found with id " + id));

        return WorkoutResponse.fromEntity(workout);
    }

    // Create
    @Transactional
    public WorkoutResponse createWorkout(@Valid WorkoutRequest request) {
        User currentUser = getCurrentUser();

        Workout workout = Workout.builder()
                .name(request.getName())
                .description(request.getDescription())
                .muscleGroup(request.getMuscleGroup())
                .difficulty(request.getDifficulty())
                .durationMinutes(request.getDurationMinutes())
                .caloriesBurned(request.getCaloriesBurned())
                .createdBy(currentUser)
                .build();

        Workout savedWorkout = workoutRepository.save(workout);

        return WorkoutResponse.fromEntity(savedWorkout);
    }

    // Update
    @Transactional
    public WorkoutResponse updateWorkout(Long id, @Valid WorkoutRequest request) {
        Workout workout = workoutRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workout not found with id " + id));

        workout.setName(request.getName());
        workout.setDescription(request.getDescription());
        workout.setMuscleGroup(request.getMuscleGroup());
        workout.setDifficulty(request.getDifficulty());
        workout.setDurationMinutes(request.getDurationMinutes());
        workout.setCaloriesBurned(request.getCaloriesBurned());

        Workout savedWorkout = workoutRepository.save(workout);

        return WorkoutResponse.fromEntity(savedWorkout);
    }

    // Delete
    @Transactional
    public void deleteWorkout(Long id) {
        if (!workoutRepository.existsById(id)) {
            throw new ResourceNotFoundException("Workout not found with id " + id);
        }

        workoutRepository.deleteById(id);
    }

    // Statistics
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalWorkouts", workoutRepository.count());

        Map<String, Long> byMuscle = new HashMap<>();
        workoutRepository.countByMuscleGroup().forEach(
                row -> byMuscle.put(row[0].toString(), (Long) row[1])
        );

        Map<String, Long> byDifficulty = new HashMap<>();
        workoutRepository.countByDifficulty().forEach(
                row -> byDifficulty.put(row[0].toString(), (Long) row[1])
        );

        stats.put("byMuscleGroup", byMuscle);
        stats.put("byDifficulty", byDifficulty);

        return stats;
    }

    // helper function
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
