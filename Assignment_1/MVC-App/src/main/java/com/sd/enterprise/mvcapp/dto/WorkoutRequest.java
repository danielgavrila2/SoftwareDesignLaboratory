package com.sd.enterprise.mvcapp.dto;

import com.sd.enterprise.mvcapp.entity.Difficulty;
import com.sd.enterprise.mvcapp.entity.MuscleGroup;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class WorkoutRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100)
    private String name;

    @Size(max = 1000)
    private String description;

    @NotNull(message = "Muscle group is required")
    private MuscleGroup muscleGroup;

    @NotNull(message = "Difficulty is required")
    private Difficulty difficulty;

    @Min(1)
    @Max(360)
    private int durationMinutes;

    @Min(0)
    private int caloriesBurned;
}
