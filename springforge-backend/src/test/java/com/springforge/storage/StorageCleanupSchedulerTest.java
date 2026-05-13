package com.springforge.storage;

import com.springforge.generator.domain.Generation;
import com.springforge.generator.domain.GenerationRepository;
import com.springforge.generator.domain.GenerationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StorageCleanupSchedulerTest {

    @Mock
    private StorageService storageService;

    @Mock
    private GenerationRepository generationRepository;

    private StorageCleanupScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new StorageCleanupScheduler(generationRepository, storageService, 30);
    }

    @Test
    void cleanup_shouldDoNothingWhenNoExpiredGenerations() {
        when(generationRepository.findByStatusAndCreatedAtBefore(eq(GenerationStatus.COMPLETED), any()))
                .thenReturn(Collections.emptyList());

        scheduler.cleanupExpiredGenerations();

        verify(storageService, never()).delete(any());
    }

    @Test
    void cleanup_shouldDeleteStorageForExpiredGenerations() {
        Generation gen = mock(Generation.class);
        when(gen.getOutputPath()).thenReturn("generations/test-uuid.zip");
        when(generationRepository.findByStatusAndCreatedAtBefore(eq(GenerationStatus.COMPLETED), any()))
                .thenReturn(List.of(gen));

        scheduler.cleanupExpiredGenerations();

        verify(storageService).delete("generations/test-uuid.zip");
    }

    @Test
    void cleanup_shouldSkipGenerationsWithNullPath() {
        Generation gen = mock(Generation.class);
        when(gen.getOutputPath()).thenReturn(null);
        when(generationRepository.findByStatusAndCreatedAtBefore(eq(GenerationStatus.COMPLETED), any()))
                .thenReturn(List.of(gen));

        scheduler.cleanupExpiredGenerations();

        verify(storageService, never()).delete(any());
    }

    @Test
    void cleanup_shouldContinueOnDeleteFailure() {
        Generation gen1 = mock(Generation.class);
        Generation gen2 = mock(Generation.class);
        when(gen1.getOutputPath()).thenReturn("file1.zip");
        when(gen2.getOutputPath()).thenReturn("file2.zip");
        when(generationRepository.findByStatusAndCreatedAtBefore(eq(GenerationStatus.COMPLETED), any()))
                .thenReturn(List.of(gen1, gen2));
        doThrow(new RuntimeException("Failed")).when(storageService).delete("file1.zip");

        scheduler.cleanupExpiredGenerations();

        verify(storageService).delete("file1.zip");
        verify(storageService).delete("file2.zip");
    }
}
