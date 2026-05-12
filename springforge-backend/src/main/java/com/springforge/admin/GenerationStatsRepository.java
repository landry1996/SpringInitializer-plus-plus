package com.springforge.admin;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface GenerationStatsRepository extends JpaRepository<GenerationStats, String> {

    long countByGeneratedAtAfter(LocalDateTime after);

    long countBySuccessTrue();

    @Query("SELECT g.architectureType, COUNT(g) FROM GenerationStats g GROUP BY g.architectureType ORDER BY COUNT(g) DESC")
    List<Object[]> countByArchitectureType();

    @Query("SELECT g.javaVersion, COUNT(g) FROM GenerationStats g GROUP BY g.javaVersion ORDER BY COUNT(g) DESC")
    List<Object[]> countByJavaVersion();

    @Query("SELECT CAST(g.generatedAt AS date), COUNT(g) FROM GenerationStats g WHERE g.generatedAt >= :since GROUP BY CAST(g.generatedAt AS date) ORDER BY CAST(g.generatedAt AS date)")
    List<Object[]> dailyStats(LocalDateTime since);
}
