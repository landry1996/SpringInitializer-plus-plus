package com.springforge.user.application;

import com.springforge.user.domain.User;

import java.util.UUID;

public record UserResponse(UUID id, String email, String firstName, String lastName, String role) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getFirstName(),
                user.getLastName(), user.getRole().name());
    }
}
