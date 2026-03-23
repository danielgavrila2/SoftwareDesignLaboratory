package com.sd.enterprise.mvcapp.config;

import com.sd.enterprise.mvcapp.entity.*;
import com.sd.enterprise.mvcapp.repository.UserRepository;
import com.sd.enterprise.mvcapp.repository.WorkoutRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("dev")
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final WorkoutRepository workoutRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) return;

        log.info("Seeding initial data...");

        // Create users
        User admin = User.builder()
                .firstName("Admin")
                .lastName("FitCore")
                .email("admin@fitcore.com")
                .password(passwordEncoder.encode("admin123"))
                .role(Role.ADMIN)
                .enabled(true)
                .build();

        User user1 = User.builder()
                .firstName("Alex")
                .lastName("Johnson")
                .email("alex@example.com")
                .password(passwordEncoder.encode("user123"))
                .role(Role.USER)
                .enabled(true)
                .build();

        User user2 = User.builder()
                .firstName("Maria")
                .lastName("Popescu")
                .email("maria@example.com")
                .password(passwordEncoder.encode("user123"))
                .role(Role.USER)
                .enabled(true)
                .build();

        userRepository.saveAll(List.of(admin, user1, user2));

        // Create sample workouts
        List<Workout> workouts = List.of(
                Workout.builder().name("Push-Up Power").description("Classic chest and triceps workout using bodyweight push-ups.")
                        .muscleGroup(MuscleGroup.CHEST).difficulty(Difficulty.BEGINNER)
                        .durationMinutes(20).caloriesBurned(150).createdBy(admin).build(),

                Workout.builder().name("Squat Challenge").description("Build powerful legs with progressive squat variations.")
                        .muscleGroup(MuscleGroup.LEGS).difficulty(Difficulty.INTERMEDIATE)
                        .durationMinutes(30).caloriesBurned(250).createdBy(admin).build(),

                Workout.builder().name("HIIT Cardio Blast").description("High intensity interval training for maximum fat burn.")
                        .muscleGroup(MuscleGroup.CARDIO).difficulty(Difficulty.ADVANCED)
                        .durationMinutes(45).caloriesBurned(500).createdBy(admin).build(),

                Workout.builder().name("Core Crusher").description("Targeted ab and core strengthening exercises.")
                        .muscleGroup(MuscleGroup.CORE).difficulty(Difficulty.INTERMEDIATE)
                        .durationMinutes(25).caloriesBurned(180).createdBy(admin).build(),

                Workout.builder().name("Pull-Up Progression").description("Back and biceps workout focusing on pull-up technique.")
                        .muscleGroup(MuscleGroup.BACK).difficulty(Difficulty.INTERMEDIATE)
                        .durationMinutes(35).caloriesBurned(220).createdBy(user1).build(),

                Workout.builder().name("Shoulder Sculpt").description("Comprehensive shoulder workout with pressing and lateral movements.")
                        .muscleGroup(MuscleGroup.SHOULDERS).difficulty(Difficulty.BEGINNER)
                        .durationMinutes(30).caloriesBurned(190).createdBy(admin).build(),

                Workout.builder().name("Full Body Burn").description("Complete full body workout for beginners.")
                        .muscleGroup(MuscleGroup.FULL_BODY).difficulty(Difficulty.BEGINNER)
                        .durationMinutes(40).caloriesBurned(320).createdBy(admin).build(),

                Workout.builder().name("Arm Day").description("Bicep and tricep isolation exercises for arm growth.")
                        .muscleGroup(MuscleGroup.ARMS).difficulty(Difficulty.BEGINNER)
                        .durationMinutes(25).caloriesBurned(160).createdBy(user1).build(),

                Workout.builder().name("Deadlift Mastery").description("Advanced back and leg workout centered around the deadlift.")
                        .muscleGroup(MuscleGroup.BACK).difficulty(Difficulty.ADVANCED)
                        .durationMinutes(50).caloriesBurned(400).createdBy(admin).build(),

                Workout.builder().name("5K Runner Prep").description("Running-focused cardio training to prepare for a 5K race.")
                        .muscleGroup(MuscleGroup.CARDIO).difficulty(Difficulty.INTERMEDIATE)
                        .durationMinutes(40).caloriesBurned(380).createdBy(user2).build()
        );

        workoutRepository.saveAll(workouts);
        log.info("Seeded {} users and {} workouts.", 3, workouts.size());
    }
}
