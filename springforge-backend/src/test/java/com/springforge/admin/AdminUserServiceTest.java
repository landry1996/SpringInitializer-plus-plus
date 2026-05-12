package com.springforge.admin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    @Mock
    private AdminUserRepository repository;

    @InjectMocks
    private AdminUserService service;

    private AdminUser sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new AdminUser();
        sampleUser.setId("user-1");
        sampleUser.setUsername("testuser");
        sampleUser.setEmail("test@example.com");
        sampleUser.setRole(UserRole.USER);
        sampleUser.setActive(true);
    }

    @Test
    void shouldListUsers() {
        Page<AdminUser> page = new PageImpl<>(List.of(sampleUser));
        when(repository.findAll(any(PageRequest.class))).thenReturn(page);

        Page<AdminUser> result = service.listUsers(PageRequest.of(0, 20));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUsername()).isEqualTo("testuser");
    }

    @Test
    void shouldCreateUser() {
        when(repository.save(any())).thenReturn(sampleUser);

        AdminUser result = service.createUser(sampleUser);

        assertThat(result).isNotNull();
        verify(repository).save(sampleUser);
    }

    @Test
    void shouldUpdateUser() {
        when(repository.findById("user-1")).thenReturn(Optional.of(sampleUser));
        when(repository.save(any())).thenReturn(sampleUser);

        AdminUser updated = new AdminUser();
        updated.setUsername("newname");
        updated.setEmail("new@example.com");

        AdminUser result = service.updateUser("user-1", updated);

        assertThat(result.getUsername()).isEqualTo("newname");
        assertThat(result.getEmail()).isEqualTo("new@example.com");
    }

    @Test
    void shouldChangeRole() {
        when(repository.findById("user-1")).thenReturn(Optional.of(sampleUser));
        when(repository.save(any())).thenReturn(sampleUser);

        service.changeRole("user-1", UserRole.ADMIN);

        assertThat(sampleUser.getRole()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    void shouldDeactivateUser() {
        when(repository.findById("user-1")).thenReturn(Optional.of(sampleUser));
        when(repository.save(any())).thenReturn(sampleUser);

        service.deactivateUser("user-1");

        assertThat(sampleUser.isActive()).isFalse();
    }

    @Test
    void shouldThrowOnUpdateNonExistentUser() {
        when(repository.findById("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateUser("unknown", new AdminUser()))
            .isInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldGetActiveUserCount() {
        when(repository.countByActiveTrue()).thenReturn(42L);

        assertThat(service.getActiveUserCount()).isEqualTo(42L);
    }
}
