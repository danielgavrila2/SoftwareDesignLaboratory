package com.fitness.userservice;

import com.fitness.userservice.cqrs.commands.RegisterUserCommand;
import com.fitness.userservice.cqrs.commands.RegisterUserCommandHandler;
import com.fitness.userservice.dto.UserResponse;
import com.fitness.userservice.models.User;
import com.fitness.userservice.models.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class RegisterUserCommandHandlerTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RegisterUserCommandHandler handler;

    @Test
    public void testHandleRegisterUserCommand() {
        RegisterUserCommand command = new RegisterUserCommand(
                "test@example.com", "password", "John", "Doe", "keycloak-123"
        );

        User savedUser = new User();
        savedUser.setId("1");
        savedUser.setEmail("test@example.com");
        savedUser.setPassword("password");
        savedUser.setFirstName("John");
        savedUser.setLastName("Doe");
        savedUser.setRole(UserRole.USER);
        
        Mockito.when(userRepository.findByEmail(command.getEmail())).thenReturn(null);
        Mockito.when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponse response = handler.handle(command);

        assertEquals("test@example.com", response.getEmail());
        assertEquals("John", response.getFirstName());
    }
}
