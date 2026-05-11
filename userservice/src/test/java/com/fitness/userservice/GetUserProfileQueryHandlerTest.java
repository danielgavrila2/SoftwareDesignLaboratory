package com.fitness.userservice;

import com.fitness.userservice.UserRepository;
import com.fitness.userservice.cqrs.queries.GetUserProfileQuery;
import com.fitness.userservice.cqrs.queries.GetUserProfileQueryHandler;
import com.fitness.userservice.dto.UserResponse;
import com.fitness.userservice.models.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
public class GetUserProfileQueryHandlerTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GetUserProfileQueryHandler handler;

    @Test
    public void testHandleGetUserProfile_Success_ById() {
        // Arrange
        GetUserProfileQuery query = new GetUserProfileQuery("user123");
        User user = new User();
        user.setId("user123");
        user.setEmail("test@test.com");
        
        Mockito.when(userRepository.findById("user123")).thenReturn(Optional.of(user));

        // Act
        UserResponse response = handler.handle(query);

        // Assert
        assertEquals("test@test.com", response.getEmail());
    }

    @Test
    public void testHandleGetUserProfile_Success_ByEmail() {
        // Arrange
        GetUserProfileQuery query = new GetUserProfileQuery("test@test.com");
        User user = new User();
        user.setId("user123");
        user.setEmail("test@test.com");
        
        Mockito.when(userRepository.findById("test@test.com")).thenReturn(Optional.empty());
        Mockito.when(userRepository.findByKeycloakId("test@test.com")).thenReturn(null);
        Mockito.when(userRepository.findByEmail("test@test.com")).thenReturn(user);

        // Act
        UserResponse response = handler.handle(query);

        // Assert
        assertEquals("user123", response.getId());
        assertEquals("test@test.com", response.getEmail());
    }
}
