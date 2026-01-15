package com.example.bank_app.users.infrastructure;

import com.example.bank_app.users.domain.User;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JpaUserRepositoryAdapterTest {

    @Test
    void adapter_delegatesToSpringDataRepository() {
        UserJpaRepository jpa = mock(UserJpaRepository.class);
        JpaUserRepositoryAdapter adapter = new JpaUserRepositoryAdapter(jpa);

        UUID id = UUID.randomUUID();
        User u = mock(User.class);

        when(jpa.findById(id)).thenReturn(Optional.of(u));
        when(jpa.save(u)).thenReturn(u);
        when(jpa.findAll()).thenReturn(List.of(u));

        assertEquals(Optional.of(u), adapter.findById(id));
        assertSame(u, adapter.save(u));
        assertEquals(List.of(u), adapter.findAll());

        verify(jpa).findById(id);
        verify(jpa).save(u);
        verify(jpa).findAll();
    }
}
