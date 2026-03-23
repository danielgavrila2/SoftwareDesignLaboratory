package com.sd.enterprise.mvcapp;

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
import com.sd.enterprise.mvcapp.services.WorkoutService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WorkoutService Unit Tests")
class WorkoutServiceTest {

    @Mock
    private WorkoutRepository workoutRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks
    private WorkoutService workoutService;

    private User adminUser;
    private User regularUser;
    private Workout sampleWorkout;

    @BeforeEach
    void setUp() {
        adminUser   = TestDataFactory.buildAdminUser();
        regularUser = TestDataFactory.buildRegularUser();
        sampleWorkout = TestDataFactory.buildWorkout(adminUser);
        mockSecurityContext(adminUser.getEmail());
    }

    // ─── getWorkouts ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getWorkouts()")
    class GetWorkouts {

        @Test
        @DisplayName("returns paginated results with no filters")
        void returnsPagedResults() {
            Page<Workout> page = new PageImpl<>(List.of(sampleWorkout), PageRequest.of(0, 10), 1);
            when(workoutRepository.findWithFilters(isNull(), isNull(), isNull(), isNull(), isNull(), any()))
                    .thenReturn(page);

            PageResponse<WorkoutResponse> result =
                    workoutService.getWorkouts(null, null, null, null, null, 0, 10, "createdAt", "desc");

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getPage()).isZero();
            assertThat(result.getContent().get(0).getName()).isEqualTo("Push-Up Power");
        }

        @Test
        @DisplayName("passes filter parameters to repository")
        void passesFiltersToRepository() {
            Page<Workout> emptyPage = Page.empty();
            when(workoutRepository.findWithFilters(eq("push"), eq(MuscleGroup.CHEST),
                    eq(Difficulty.BEGINNER), eq(10), eq(60), any()))
                    .thenReturn(emptyPage);

            workoutService.getWorkouts("push", MuscleGroup.CHEST, Difficulty.BEGINNER, 10, 60, 0, 10, "name", "asc");

            verify(workoutRepository).findWithFilters(
                    eq("push"), eq(MuscleGroup.CHEST), eq(Difficulty.BEGINNER),
                    eq(10), eq(60), any(Pageable.class));
        }

        @Test
        @DisplayName("uses ascending sort when sortDir is 'asc'")
        void ascendingSort() {
            Page<Workout> page = new PageImpl<>(List.of(sampleWorkout));
            ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
            when(workoutRepository.findWithFilters(any(), any(), any(), any(), any(), captor.capture()))
                    .thenReturn(page);

            workoutService.getWorkouts(null, null, null, null, null, 0, 10, "name", "asc");

            Sort.Order order = captor.getValue().getSort().getOrderFor("name");
            assertThat(order).isNotNull();
            assertThat(order.isAscending()).isTrue();
        }

        @Test
        @DisplayName("returns empty page when no workouts match filters")
        void returnsEmptyPage() {
            when(workoutRepository.findWithFilters(any(), any(), any(), any(), any(), any()))
                    .thenReturn(Page.empty());

            PageResponse<WorkoutResponse> result =
                    workoutService.getWorkouts("nonexistent", null, null, null, null, 0, 10, "name", "asc");

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }
    }

    // ─── getWorkoutById ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("getWorkoutById()")
    class GetWorkoutById {

        @Test
        @DisplayName("returns workout when it exists")
        void returnsExistingWorkout() {
            when(workoutRepository.findById(1L)).thenReturn(Optional.of(sampleWorkout));

            WorkoutResponse result = workoutService.getWorkoutById(1L);

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getName()).isEqualTo("Push-Up Power");
            assertThat(result.getMuscleGroup()).isEqualTo(MuscleGroup.CHEST);
            assertThat(result.getDifficulty()).isEqualTo(Difficulty.BEGINNER);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when workout is missing")
        void throwsWhenNotFound() {
            when(workoutRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> workoutService.getWorkoutById(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    // ─── createWorkout ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("createWorkout()")
    class CreateWorkout {

        @Test
        @DisplayName("creates and returns workout with correct fields")
        void createsWorkout() {
            WorkoutRequest req = TestDataFactory.buildWorkoutRequest();
            Workout savedWorkout = Workout.builder()
                    .id(10L).name(req.getName()).description(req.getDescription())
                    .muscleGroup(req.getMuscleGroup()).difficulty(req.getDifficulty())
                    .durationMinutes(req.getDurationMinutes()).caloriesBurned(req.getCaloriesBurned())
                    .createdBy(adminUser).build();

            when(userRepository.findByEmail(adminUser.getEmail())).thenReturn(Optional.of(adminUser));
            when(workoutRepository.save(any(Workout.class))).thenReturn(savedWorkout);

            WorkoutResponse result = workoutService.createWorkout(req);

            assertThat(result.getId()).isEqualTo(10L);
            assertThat(result.getName()).isEqualTo("HIIT Blast");
            assertThat(result.getMuscleGroup()).isEqualTo(MuscleGroup.CARDIO);
            assertThat(result.getDifficulty()).isEqualTo(Difficulty.ADVANCED);
            assertThat(result.getDurationMinutes()).isEqualTo(30);
            assertThat(result.getCaloriesBurned()).isEqualTo(400);
        }

        @Test
        @DisplayName("associates workout with currently authenticated user")
        void associatesCurrentUser() {
            WorkoutRequest req = TestDataFactory.buildWorkoutRequest();
            when(userRepository.findByEmail(adminUser.getEmail())).thenReturn(Optional.of(adminUser));
            when(workoutRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            workoutService.createWorkout(req);

            ArgumentCaptor<Workout> captor = ArgumentCaptor.forClass(Workout.class);
            verify(workoutRepository).save(captor.capture());
            assertThat(captor.getValue().getCreatedBy()).isEqualTo(adminUser);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when current user does not exist")
        void throwsWhenCurrentUserMissing() {
            when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> workoutService.createWorkout(TestDataFactory.buildWorkoutRequest()))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ─── updateWorkout ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("updateWorkout()")
    class UpdateWorkout {

        @Test
        @DisplayName("updates all mutable fields and returns response")
        void updatesFields() {
            WorkoutRequest req = TestDataFactory.buildWorkoutRequest("Updated Name", MuscleGroup.BACK, Difficulty.ADVANCED);
            req.setDurationMinutes(45);
            req.setCaloriesBurned(350);

            when(workoutRepository.findById(1L)).thenReturn(Optional.of(sampleWorkout));
            when(workoutRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            WorkoutResponse result = workoutService.updateWorkout(1L, req);

            assertThat(result.getName()).isEqualTo("Updated Name");
            assertThat(result.getMuscleGroup()).isEqualTo(MuscleGroup.BACK);
            assertThat(result.getDifficulty()).isEqualTo(Difficulty.ADVANCED);
            assertThat(result.getDurationMinutes()).isEqualTo(45);
            assertThat(result.getCaloriesBurned()).isEqualTo(350);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException for unknown id")
        void throwsForUnknownId() {
            when(workoutRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> workoutService.updateWorkout(99L, TestDataFactory.buildWorkoutRequest()))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }

        @Test
        @DisplayName("calls save with the mutated entity")
        void callsSave() {
            when(workoutRepository.findById(1L)).thenReturn(Optional.of(sampleWorkout));
            when(workoutRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            workoutService.updateWorkout(1L, TestDataFactory.buildWorkoutRequest());

            verify(workoutRepository, times(1)).save(any(Workout.class));
        }
    }

    // ─── deleteWorkout ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("deleteWorkout()")
    class DeleteWorkout {

        @Test
        @DisplayName("deletes workout when it exists")
        void deletesExistingWorkout() {
            when(workoutRepository.existsById(1L)).thenReturn(true);

            workoutService.deleteWorkout(1L);

            verify(workoutRepository).deleteById(1L);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when workout not found")
        void throwsWhenNotFound() {
            when(workoutRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> workoutService.deleteWorkout(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");

            verify(workoutRepository, never()).deleteById(any());
        }
    }

    // ─── getStats ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getStats()")
    class GetStats {

        @Test
        @DisplayName("returns total workout count")
        void returnsTotalCount() {
            when(workoutRepository.count()).thenReturn(10L);
            when(workoutRepository.countByMuscleGroup()).thenReturn(List.of());
            when(workoutRepository.countByDifficulty()).thenReturn(List.of());

            Map<String, Object> stats = workoutService.getStats();

            assertThat(stats.get("totalWorkouts")).isEqualTo(10L);
        }

        @Test
        @DisplayName("returns breakdown by muscle group")
        void returnsMuscleGroupBreakdown() {
            when(workoutRepository.count()).thenReturn(5L);
            when(workoutRepository.countByMuscleGroup())
                    .thenReturn(List.of(new Object[]{MuscleGroup.CHEST, 3L}, new Object[]{MuscleGroup.BACK, 2L}));
            when(workoutRepository.countByDifficulty()).thenReturn(List.of());

            Map<String, Object> stats = workoutService.getStats();

            @SuppressWarnings("unchecked")
            Map<String, Long> byMuscle = (Map<String, Long>) stats.get("byMuscleGroup");
            assertThat(byMuscle).containsEntry("CHEST", 3L).containsEntry("BACK", 2L);
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private void mockSecurityContext(String email) {
        Authentication auth = mock(Authentication.class);
        SecurityContext ctx = mock(SecurityContext.class);
        when(auth.getName()).thenReturn(email);
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);
    }
}
