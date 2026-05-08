package com.springforge.generator.domain.pipeline;

import java.util.List;

public record StepResult(boolean success, List<String> errors) {
    public static StepResult ok() { return new StepResult(true, List.of()); }
    public static StepResult failed(String error) { return new StepResult(false, List.of(error)); }
    public static StepResult failed(List<String> errors) { return new StepResult(false, errors); }
}
