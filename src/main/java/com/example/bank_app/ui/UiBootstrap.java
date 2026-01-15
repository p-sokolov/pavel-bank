package com.example.bank_app.ui;

import com.example.bank_app.users.application.UserRepository;
import com.example.bank_app.users.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Creates 2 demo users on first start (when DB is empty),
 * so you can immediately log in by UID and click around.
 */
@Component
public class UiBootstrap implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(UiBootstrap.class);

    private final UserRepository userRepository;

    public UiBootstrap(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        List<User> existing = userRepository.findAll();
        if (!existing.isEmpty()) {
            return;
        }

        User u1 = userRepository.save(new User("Alice"));
        User u2 = userRepository.save(new User("Bob"));

        log.info("=== Demo users created ===");
        log.info("Alice UID: {}", u1.getId());
        log.info("Bob   UID: {}", u2.getId());
        log.info("==========================");
    }
}
