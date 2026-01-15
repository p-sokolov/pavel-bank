package com.example.bank_app.users.application;

import com.example.bank_app.users.domain.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    Optional<User> findById(UUID id);
    User save(User user);
    List<User> findAll();
}