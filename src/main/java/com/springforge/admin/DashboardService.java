package com.springforge.admin;

import com.springforge.marketplace.BlueprintRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final GenerationStatsRepository statsRepository;
    private final AdminUserRepository userRepository;
    private final BlueprintRepository blueprintRepository;

    public DashboardService(GenerationStatsRepository statsRepository,
                           AdminUserRepository userRepository,
                           BlueprintRepository blueprintRepository) {
        this.statsRepository = statsRepository;
        this.userRepository = userRepository;
        this.blueprintRepository = blueprintRepository;
    }

    public DashboardStats getDashboardStats() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime startOfWeek = now.minusDays(7);
        LocalDateTime startOfMonth = now.minusDays(30);

        long totalProjects = statsRepository.count();
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByActiveTrue();
        long totalBlueprints = blueprintRepository.count();
        long projectsToday = statsRepository.countByGeneratedAtAfter(startOfDay);
        long projectsThisWeek = statsRepository.countByGeneratedAtAfter(startOfWeek);
        long projectsThisMonth = statsRepository.countByGeneratedAtAfter(startOfMonth);

        Map<String, Long> projectsByArchitecture = statsRepository.countByArchitectureType().stream()
            .collect(Collectors.toMap(
                row -> (String) row[0],
                row -> (Long) row[1],
                (a, b) -> a,
                LinkedHashMap::new
            ));

        Map<String, Long> projectsByJavaVersion = statsRepository.countByJavaVersion().stream()
            .collect(Collectors.toMap(
                row -> (String) row[0],
                row -> (Long) row[1],
                (a, b) -> a,
                LinkedHashMap::new
            ));

        List<DailyStats> dailyStats = statsRepository.dailyStats(startOfMonth).stream()
            .map(row -> new DailyStats((LocalDate) row[0], (Long) row[1]))
            .toList();

        return new DashboardStats(
            totalProjects, totalUsers, activeUsers, totalBlueprints,
            projectsToday, projectsThisWeek, projectsThisMonth,
            projectsByArchitecture, projectsByJavaVersion,
            Map.of(),
            dailyStats
        );
    }
}
