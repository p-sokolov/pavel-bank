package com.example.bank_app.ui;

import com.example.bank_app.common.api.NotFoundException;
import com.example.bank_app.users.application.UserRepository;
import com.example.bank_app.users.domain.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/ui")
public class UiAuthController {

    private final UserRepository userRepository;

    public UiAuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping({"", "/", "/login"})
    public String loginPage(Model model, HttpSession session) {
        model.addAttribute("currentUserId", UiSession.getUserId(session).orElse(null));
        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam("userId") UUID userId, HttpSession session) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        UiSession.setUserId(session, user.getId());
        return "redirect:/ui/dashboard";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        UiSession.clear(session);
        return "redirect:/ui/login";
    }

    @PostMapping("/users")
    public String createUser(@RequestParam("name") String name) {
        userRepository.save(new User(name));
        return "redirect:/ui/login";
    }

    /**
     * Convenience endpoint: clears the session and returns to login.
     */
    @PostMapping("/reset-session")
    public String resetSession(HttpSession session) {
        UiSession.clear(session);
        return "redirect:/ui/login";
    }
}
