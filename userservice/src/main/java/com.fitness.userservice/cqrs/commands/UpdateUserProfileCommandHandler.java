package com.fitness.userservice.cqrs.commands;

import com.fitness.userservice.UserRepository;
import com.fitness.userservice.dto.UserResponse;
import com.fitness.userservice.models.User;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class UpdateUserProfileCommandHandler {
    private final UserRepository userRepository;

    @Transactional
    public UserResponse handle(UpdateUserProfileCommand command) {
        User user = userRepository.findById(command.getUserId()).orElse(null);
        if (user == null) {
            user = userRepository.findByKeycloakId(command.getUserId());
        }
        if (user == null && command.getUserId().contains("@")) {
            user = userRepository.findByEmail(command.getUserId());
        }
        
        if (user == null) {
            throw new RuntimeException("User not found with identifier: " + command.getUserId());
        }

        user.setHeight(command.getHeight());
        user.setWeight(command.getWeight());
        user.setAge(command.getAge());

        User savedUser = userRepository.save(user);

        return mapUserToResponse(savedUser);
    }

    private UserResponse mapUserToResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(String.valueOf(user.getId()));
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setHeight(user.getHeight());
        response.setWeight(user.getWeight());
        response.setAge(user.getAge());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        return response;
    }
}
