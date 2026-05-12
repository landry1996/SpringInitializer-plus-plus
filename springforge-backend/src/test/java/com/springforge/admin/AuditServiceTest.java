package com.springforge.admin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditLogRepository repository;

    @InjectMocks
    private AuditService service;

    @Test
    void shouldLogAuditEvent() {
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.log("user-1", "PROJECT_GENERATED", "Generated demo project", "192.168.1.1");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(repository).save(captor.capture());

        AuditLog saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo("user-1");
        assertThat(saved.getAction()).isEqualTo("PROJECT_GENERATED");
        assertThat(saved.getDetails()).isEqualTo("Generated demo project");
        assertThat(saved.getIpAddress()).isEqualTo("192.168.1.1");
    }

    @Test
    void shouldGetRecentLogs() {
        AuditLog log = new AuditLog();
        log.setAction("USER_LOGIN");
        Page<AuditLog> page = new PageImpl<>(List.of(log));
        when(repository.findAllByOrderByTimestampDesc(any())).thenReturn(page);

        Page<AuditLog> result = service.getRecentLogs(PageRequest.of(0, 50));

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void shouldFilterByAction() {
        Page<AuditLog> page = new PageImpl<>(List.of());
        when(repository.findByActionOrderByTimestampDesc(eq("USER_LOGIN"), any())).thenReturn(page);

        Page<AuditLog> result = service.getLogsByAction("USER_LOGIN", PageRequest.of(0, 50));

        assertThat(result).isNotNull();
        verify(repository).findByActionOrderByTimestampDesc(eq("USER_LOGIN"), any());
    }

    @Test
    void shouldFilterByUser() {
        Page<AuditLog> page = new PageImpl<>(List.of());
        when(repository.findByUserIdOrderByTimestampDesc(eq("user-1"), any())).thenReturn(page);

        Page<AuditLog> result = service.getLogsByUser("user-1", PageRequest.of(0, 50));

        assertThat(result).isNotNull();
        verify(repository).findByUserIdOrderByTimestampDesc(eq("user-1"), any());
    }
}
