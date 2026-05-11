package com.fitness.userservice.cqrs.commands;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RegisterUserCommand {
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String keycloakId;
}
