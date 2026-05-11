package com.fitness.userservice.cqrs.commands;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdateUserProfileCommand {
    private String userId;
    private Double height;
    private Double weight;
    private Integer age;
}
