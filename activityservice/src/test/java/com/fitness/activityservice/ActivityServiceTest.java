package com.fitness.activityservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.activityservice.dto.ActivityRequest;
import com.fitness.activityservice.model.Activity;
import com.fitness.activityservice.model.ActivityType;
import com.fitness.activityservice.service.ActivityService;
import com.fitness.activityservice.service.UserValidationservice;
import com.fitness.activityservice.sto.ActivityResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
public class ActivityServiceTest {

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private UserValidationservice userValidationservice;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ActivityService activityService;

    @Test
    public void testTrackActivity_Success() {
        ActivityRequest request = new ActivityRequest();
        request.setUserId("user123");
        request.setActivityType(ActivityType.RUNNING);
        request.setDuration(30);
        request.setCaloriesBurned(300);
        request.setStartTime(LocalDateTime.now());

        Activity savedActivity = Activity.builder()
                .id("act1")
                .userId("user123")
                .activityType(ActivityType.RUNNING)
                .duration(30)
                .caloriesBurned(300)
                .build();

        Mockito.when(userValidationservice.validateUser("user123")).thenReturn(true);
        Mockito.when(activityRepository.save(any(Activity.class))).thenReturn(savedActivity);

        ActivityResponse response = activityService.trackActivity(request);

        assertEquals("act1", response.getId());
        assertEquals("user123", response.getUserId());
        assertEquals(30, response.getDuration());
        
        // verify kafka was called
        Mockito.verify(kafkaTemplate).send(anyString(), any(Activity.class));
    }

    @Test
    public void testTrackActivity_InvalidUser() {
        ActivityRequest request = new ActivityRequest();
        request.setUserId("invalidUser");

        Mockito.when(userValidationservice.validateUser("invalidUser")).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            activityService.trackActivity(request);
        });

        assertEquals("Invalid user ID: invalidUser", exception.getMessage());
    }

    @Test
    public void testDeleteActivity_Unauthorized() {
        Activity existing = Activity.builder().id("act1").userId("user123").build();
        Mockito.when(activityRepository.findById("act1")).thenReturn(Optional.of(existing));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            activityService.deleteActivity("act1", "wrongUser");
        });

        assertEquals("Unauthorized to delete this activity", exception.getMessage());
    }
}
