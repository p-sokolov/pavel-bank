package com.example.bank_app.ui;

import com.example.bank_app.users.application.UserRepository;
import com.example.bank_app.users.domain.User;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.*;

class UiBootstrapTest {

    @Test
    void run_createsDemoUsersOnlyWhenDbEmpty() throws Exception {
        UserRepository repo = mock(UserRepository.class);
        when(repo.findAll()).thenReturn(List.of());
        when(repo.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UiBootstrap bootstrap = new UiBootstrap(repo);
        bootstrap.run();

        verify(repo, times(1)).findAll();
        verify(repo, times(2)).save(any(User.class));

        // second run when users exist -> no new demo users
        reset(repo);
        when(repo.findAll()).thenReturn(List.of(new User("X")));
        bootstrap.run();
        verify(repo).findAll();
        verify(repo, never()).save(any());
    }
}
