package com.springforge.storage;

import com.springforge.generator.domain.Generation;
import com.springforge.generator.domain.GenerationRepository;
import com.springforge.generator.domain.GenerationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class StorageCleanupScheduler {

    private static final Logger log = LoggerFactory.getLogger(StorageCleanupScheduler.class);

    private final GenerationRepository generationRepository;
    private final StorageService storageService;
    private final int retentionDays;

    public StorageCleanupScheduler(GenerationRepository generationRepository,
                                   StorageService storageService,
                                   @Value("${app.storage.retention-days:30}") int retentionDays) {
        this.generationRepository = generationRepository;
        this.storageService = storageService;
        this.retentionDays = retentionDays;
    }

    @Scheduled(cron = "0 0 3 * * *")
    public void cleanupExpiredGenerations() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(retentionDays);
        List<Generation> expired = generationRepository.findByStatusAndCreatedAtBefore(
                GenerationStatus.COMPLETED, cutoff);

        int deleted = 0;
        for (Generation generation : expired) {
            try {
                String path = generation.getOutputPath();
                if (path != null && !path.isBlank()) {
                    storageService.delete(path);
                    deleted++;
                }
            } catch (Exception e) {
                log.warn("Failed to delete storage for generation {}: {}", generation.getId(), e.getMessage());
            }
        }
        if (deleted > 0) {
            log.info("Cleaned up {} expired generations older than {} days", deleted, retentionDays);
        }
    }
}
