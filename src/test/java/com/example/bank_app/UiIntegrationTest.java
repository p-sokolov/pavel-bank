package com.example.bank_app;

import com.example.bank_app.ui.UiSession;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UiIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @Test
    void ui_requiresLogin_forDashboardAndAccountPage() throws Exception {
        // Not logged in -> NotFoundException -> handled by GlobalExceptionHandler (JSON),
        // but MVC controller returns view; Spring will still go through exception handler because it's @RestControllerAdvice.
        mvc.perform(get("/ui/dashboard"))
                .andExpect(status().isNotFound());

        mvc.perform(get("/ui/accounts/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void ui_login_and_logout_flow_setsAndClearsSession() throws Exception {
        // create user via api
        String userJson = mvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(java.util.Map.of("name", "UIUser"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        UUID userId = UUID.fromString(om.readTree(userJson).get("id").asText());

        MockHttpSession session = new MockHttpSession();
        mvc.perform(post("/ui/login")
                        .session(session)
                        .param("userId", userId.toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ui/dashboard"));

        // session should contain user id after login
        Object stored = session.getAttribute(UiSession.USER_ID);
        assertNotNull(stored);

        mvc.perform(post("/ui/logout").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ui/login"));

        // after logout, session attribute removed
        assertNull(session.getAttribute(UiSession.USER_ID));
    }
}
