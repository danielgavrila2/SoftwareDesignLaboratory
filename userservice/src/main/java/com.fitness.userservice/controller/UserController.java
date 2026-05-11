package com.fitness.userservice.controller;

import com.fitness.userservice.dto.RegisterRequest;
import com.fitness.userservice.dto.UserResponse;
import com.fitness.userservice.services.UserService;
import com.fitness.userservice.cqrs.commands.RegisterUserCommand;
import com.fitness.userservice.cqrs.commands.RegisterUserCommandHandler;
import com.fitness.userservice.cqrs.queries.GetUserProfileQuery;
import com.fitness.userservice.cqrs.queries.GetUserProfileQueryHandler;
import com.fitness.userservice.cqrs.commands.UpdateUserProfileCommand;
import com.fitness.userservice.cqrs.commands.UpdateUserProfileCommandHandler;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
public class UserController {
    private UserService userService;
    private final RegisterUserCommandHandler registerUserCommandHandler;
    private final GetUserProfileQueryHandler getUserProfileQueryHandler;
    private final UpdateUserProfileCommandHandler updateUserProfileCommandHandler;

    @GetMapping("{userId}")
    public ResponseEntity<UserResponse> getUserProfile(@PathVariable String userId){
        return ResponseEntity.ok(getUserProfileQueryHandler.handle(new GetUserProfileQuery(userId)));
    }

    @PutMapping("{userId}")
    public ResponseEntity<UserResponse> updateUserProfile(@PathVariable String userId, @RequestBody UpdateUserProfileCommand command){
        command.setUserId(userId);
        return ResponseEntity.ok(updateUserProfileCommandHandler.handle(command));
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterUserCommand command = new RegisterUserCommand(
            request.getEmail(),
            request.getPassword(),
            request.getFirstName(),
            request.getLastName(),
            request.getKeycloakId()
        );
        return ResponseEntity.ok(registerUserCommandHandler.handle(command));
    }
    @GetMapping("{userId}/validate")
    public ResponseEntity<Boolean> validateUser(@PathVariable String userId){
        System.out.println("validateUser called with ID: " + userId);
        return ResponseEntity.ok(userService.existsByUserId(userId));
    }

}