package ${packageName}.config.kafka;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    List<OutboxEvent> findByPublishedFalseOrderByCreatedAtAsc();

    @Modifying
    @Query("DELETE FROM OutboxEvent e WHERE e.published = true AND e.publishedAt < :before")
    int deletePublishedBefore(Instant before);
}
