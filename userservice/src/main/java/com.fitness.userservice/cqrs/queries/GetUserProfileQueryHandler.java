package com.fitness.userservice.cqrs.queries;

import com.fitness.userservice.UserRepository;
import com.fitness.userservice.dto.UserResponse;
import com.fitness.userservice.models.User;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class GetUserProfileQueryHandler {
    private final UserRepository userRepository;

    public UserResponse handle(GetUserProfileQuery query) {
        User user = userRepository.findById(query.getUserId()).orElse(null);
        if (user == null) {
            user = userRepository.findByKeycloakId(query.getUserId());
        }
        if (user == null && query.getUserId().contains("@")) {
            user = userRepository.findByEmail(query.getUserId());
        }
        
        if (user == null) {
            throw new RuntimeException("User not found with identifier: " + query.getUserId());
        }
        return mapUserToResponse(user);
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
