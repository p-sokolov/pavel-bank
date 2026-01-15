package com.example.bank_app.users.api;

import com.example.bank_app.common.api.NotFoundException;
import com.example.bank_app.users.domain.User;
import com.example.bank_app.users.infrastructure.UserJpaRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserJpaRepository userRepository;

    public UserController(UserJpaRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping
    public UserResponse createUser(@RequestBody CreateUserRequest request) {
        User user = new User(request.name());
        return UserResponse.from(userRepository.save(user));
    }

    @GetMapping
    public List<UserResponse> all() {
        return userRepository.findAll()
                .stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public UserResponse getById(@PathVariable UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return UserResponse.from(user);
    }
}
