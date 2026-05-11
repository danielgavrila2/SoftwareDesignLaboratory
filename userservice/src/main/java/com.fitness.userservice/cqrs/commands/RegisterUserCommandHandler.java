package com.fitness.userservice.cqrs.commands;

import com.fitness.userservice.UserRepository;
import com.fitness.userservice.dto.UserResponse;
import com.fitness.userservice.models.User;
import com.fitness.userservice.models.UserRole;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class RegisterUserCommandHandler {
    private final UserRepository userRepository;

    @Transactional
    public UserResponse handle(RegisterUserCommand command) {
        User user = userRepository.findByEmail(command.getEmail());

        if (user == null) {
            user = new User();
            user.setEmail(command.getEmail());
            user.setRole(UserRole.USER); // Default role
        }

        user.setPassword(command.getPassword());
        user.setFirstName(command.getFirstName());
        user.setLastName(command.getLastName());
        user.setKeycloakId(command.getKeycloakId());

        User savedUser = userRepository.save(user);

        return mapUserToResponse(savedUser);
    }

    private UserResponse mapUserToResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(String.valueOf(user.getId()));
        response.setEmail(user.getEmail());
        response.setPassword(user.getPassword());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        return response;
    }
}
