package com.sd.enterprise.mvcapp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "workouts", indexes = {
        @Index(name = "idx_workout_muscle_group", columnList = "muscleGroup"),
        @Index(name = "idx_workout_difficulty", columnList = "difficulty"),
        @Index(name = "idx_workout_created_by", columnList = "createdBy_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Workout {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Workout name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Column(nullable = false, length = 100)
    private String name;

    @Size(max = 1000, message = "Description must be under 1000 characters")
    @Column(length = 1000)
    private String description;

    @NotNull(message = "Muscle group is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MuscleGroup muscleGroup;

    @NotNull(message = "Difficulty is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Difficulty difficulty;

    @Min(value = 1, message = "Duration must be at least 1 minute")
    @Max(value = 300, message = "Duration cannot exceed 300 minutes")
    @Column(nullable = false)
    private int durationMinutes;

    @Min(value = 0, message = "Calories burned cannot be negative")
    @Column
    private int caloriesBurned;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "createdBy_id")
    private User createdBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

}
