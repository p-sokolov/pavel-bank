package com.example.bank_app.ui;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UiSessionTest {

    @Test
    void get_set_clear_supportsUuidAndString() {
        MockHttpSession session = new MockHttpSession();

        assertEquals(Optional.empty(), UiSession.getUserId(session));

        UUID id = UUID.randomUUID();
        UiSession.setUserId(session, id);
        assertEquals(Optional.of(id), UiSession.getUserId(session));

        // emulate some frameworks storing as string
        session.setAttribute(UiSession.USER_ID, id.toString());
        assertEquals(Optional.of(id), UiSession.getUserId(session));

        session.setAttribute(UiSession.USER_ID, "not-a-uuid");
        assertEquals(Optional.empty(), UiSession.getUserId(session));

        UiSession.clear(session);
        assertEquals(Optional.empty(), UiSession.getUserId(session));
    }
}
