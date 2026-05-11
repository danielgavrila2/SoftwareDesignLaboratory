package com.fitness.userservice;

import com.fitness.userservice.UserRepository;
import com.fitness.userservice.cqrs.commands.UpdateUserProfileCommand;
import com.fitness.userservice.cqrs.commands.UpdateUserProfileCommandHandler;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
public class UpdateUserProfileCommandHandlerTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UpdateUserProfileCommandHandler handler;

    @Test
    public void testHandleUpdateUserProfile_Success() {
        // Arrange
        UpdateUserProfileCommand command = new UpdateUserProfileCommand("user123", 180.0, 75.0, 25);
        User existingUser = new User();
        existingUser.setId("user123");
        existingUser.setEmail("test@test.com");
        existingUser.setFirstName("John");
        
        Mockito.when(userRepository.findById("user123")).thenReturn(Optional.of(existingUser));
        Mockito.when(userRepository.save(any(User.class))).thenReturn(existingUser);

        // Act
        UserResponse response = handler.handle(command);

        // Assert
        assertEquals(180.0f, response.getHeight());
        assertEquals(75.0f, response.getWeight());
        assertEquals(25, response.getAge());
    }

    @Test
    public void testHandleUpdateUserProfile_UserNotFound() {
        // Arrange
        UpdateUserProfileCommand command = new UpdateUserProfileCommand("nonexistent", 180.0, 75.0, 25);
        
        Mockito.when(userRepository.findById(anyString())).thenReturn(Optional.empty());
        Mockito.when(userRepository.findByKeycloakId(anyString())).thenReturn(null);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            handler.handle(command);
        });
        
        assertEquals("User not found with identifier: nonexistent", exception.getMessage());
    }
}
