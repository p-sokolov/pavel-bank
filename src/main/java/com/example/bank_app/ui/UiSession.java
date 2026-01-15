package com.example.bank_app.ui;

import jakarta.servlet.http.HttpSession;

import java.util.Optional;
import java.util.UUID;

public final class UiSession {
    private UiSession() {}

    public static final String USER_ID = "UI_USER_ID";

    public static Optional<UUID> getUserId(HttpSession session) {
        Object v = session.getAttribute(USER_ID);
        if (v instanceof UUID u) return Optional.of(u);
        if (v instanceof String s) {
            try { return Optional.of(UUID.fromString(s)); } catch (Exception ignored) {}
        }
        return Optional.empty();
    }

    public static void setUserId(HttpSession session, UUID userId) {
        session.setAttribute(USER_ID, userId);
    }

    public static void clear(HttpSession session) {
        session.removeAttribute(USER_ID);
    }
}
