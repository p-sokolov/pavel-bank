package com.example.bank_app.users.api;

import com.example.bank_app.users.domain.User;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String name
) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getName());
    }
}
