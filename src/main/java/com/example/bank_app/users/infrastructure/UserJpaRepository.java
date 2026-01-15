package com.example.bank_app.users.infrastructure;

import com.example.bank_app.users.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserJpaRepository extends JpaRepository<User, UUID> {
}
