package com.example.bank_app.users.infrastructure;

import com.example.bank_app.users.application.UserRepository;
import com.example.bank_app.users.domain.User;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaUserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository jpa;

    public JpaUserRepositoryAdapter(UserJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpa.findById(id);
    }

    @Override
    public User save(User user) {
        return jpa.save(user);
    }

    @Override
    public List<User> findAll() {
        return jpa.findAll();
    }
}
