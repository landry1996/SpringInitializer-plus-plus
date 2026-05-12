package com.springforge.generator.application;

import java.util.List;

public record DependencyCheckResult(
    boolean valid,
    List<String> conflicts,
    List<String> suggestions
) {
    public static DependencyCheckResult ok(List<String> suggestions) {
        return new DependencyCheckResult(true, List.of(), suggestions);
    }

    public static DependencyCheckResult withConflicts(List<String> conflicts, List<String> suggestions) {
        return new DependencyCheckResult(false, conflicts, suggestions);
    }
}
