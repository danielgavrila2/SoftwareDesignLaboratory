package com.sd.enterprise.mvcapp;

import com.sd.enterprise.mvcapp.dto.LoginRequest;
import com.sd.enterprise.mvcapp.dto.RegisterRequest;
import com.sd.enterprise.mvcapp.dto.WorkoutRequest;
import com.sd.enterprise.mvcapp.entity.*;

import java.time.LocalDateTime;

public final class TestDataFactory {

    private TestDataFactory() {}

    // ─── Users ───────────────────────────────────────────────────────────────

    public static User buildAdminUser() {
        return User.builder()
                .id(1L)
                .firstName("Admin")
                .lastName("Test")
                .email("admin@test.com")
                .password("$2a$10$hashed_password_placeholder")
                .role(Role.ADMIN)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static User buildRegularUser() {
        return User.builder()
                .id(2L)
                .firstName("Regular")
                .lastName("User")
                .email("user@test.com")
                .password("$2a$10$hashed_password_placeholder")
                .role(Role.USER)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static User buildDisabledUser() {
        return User.builder()
                .id(3L)
                .firstName("Disabled")
                .lastName("User")
                .email("disabled@test.com")
                .password("$2a$10$hashed_password_placeholder")
                .role(Role.USER)
                .enabled(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ─── Workouts ────────────────────────────────────────────────────────────

    public static Workout buildWorkout(User createdBy) {
        return Workout.builder()
                .id(1L)
                .name("Push-Up Power")
                .description("Classic chest workout")
                .muscleGroup(MuscleGroup.CHEST)
                .difficulty(Difficulty.BEGINNER)
                .durationMinutes(20)
                .caloriesBurned(150)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .createdBy(createdBy)
                .build();
    }

    public static Workout buildWorkout(Long id, String name, MuscleGroup muscleGroup,
                                       Difficulty difficulty, int duration, User createdBy) {
        return Workout.builder()
                .id(id)
                .name(name)
                .description("Test description for " + name)
                .muscleGroup(muscleGroup)
                .difficulty(difficulty)
                .durationMinutes(duration)
                .caloriesBurned(duration * 7)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .createdBy(createdBy)
                .build();
    }

    // ─── DTOs ────────────────────────────────────────────────────────────────

    public static WorkoutRequest buildWorkoutRequest() {
        WorkoutRequest req = new WorkoutRequest();
        req.setName("HIIT Blast");
        req.setDescription("High intensity interval training");
        req.setMuscleGroup(MuscleGroup.CARDIO);
        req.setDifficulty(Difficulty.ADVANCED);
        req.setDurationMinutes(30);
        req.setCaloriesBurned(400);
        return req;
    }

    public static WorkoutRequest buildWorkoutRequest(String name, MuscleGroup mg, Difficulty diff) {
        WorkoutRequest req = new WorkoutRequest();
        req.setName(name);
        req.setDescription("Description for " + name);
        req.setMuscleGroup(mg);
        req.setDifficulty(diff);
        req.setDurationMinutes(25);
        req.setCaloriesBurned(200);
        return req;
    }

    public static RegisterRequest buildRegisterRequest() {
        RegisterRequest req = new RegisterRequest();
        req.setFirstName("John");
        req.setLastName("Doe");
        req.setEmail("john.doe@test.com");
        req.setPassword("password123");
        return req;
    }

    public static RegisterRequest buildRegisterRequest(String email) {
        RegisterRequest req = buildRegisterRequest();
        req.setEmail(email);
        return req;
    }

    public static LoginRequest buildLoginRequest(String email, String password) {
        LoginRequest req = new LoginRequest();
        req.setEmail(email);
        req.setPassword(password);
        return req;
    }
}
