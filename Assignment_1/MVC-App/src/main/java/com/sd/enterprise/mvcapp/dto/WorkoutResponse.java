package com.sd.enterprise.mvcapp.dto;

import com.sd.enterprise.mvcapp.entity.Difficulty;
import com.sd.enterprise.mvcapp.entity.MuscleGroup;
import com.sd.enterprise.mvcapp.entity.Workout;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorkoutResponse {
    private Long id;
    private String name;
    private String description;
    private MuscleGroup muscleGroup;
    private Difficulty difficulty;
    private int durationMinutes;
    private int caloriesBurned;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdByName;

    public static WorkoutResponse fromEntity(Workout w) {
        return WorkoutResponse.builder()
                .id(w.getId())
                .name(w.getName())
                .description(w.getDescription())
                .muscleGroup(w.getMuscleGroup())
                .difficulty(w.getDifficulty())
                .durationMinutes(w.getDurationMinutes())
                .caloriesBurned(w.getCaloriesBurned())
                .createdAt(w.getCreatedAt())
                .updatedAt(w.getUpdatedAt())
                .createdByName(w.getCreatedBy() != null ? w.getCreatedBy().getFullName() : "System")
                .build();
    }
}
