package com.springforge.shared.exception;

import java.time.LocalDateTime;
import java.util.List;

public record ApiError(
        int status,
        String error,
        String message,
        String path,
        LocalDateTime timestamp,
        List<FieldError> fieldErrors
) {
    public record FieldError(String field, String message) {}

    public static ApiError of(int status, String error, String message, String path) {
        return new ApiError(status, error, message, path, LocalDateTime.now(), List.of());
    }

    public static ApiError withFieldErrors(int status, String error, String message, String path, List<FieldError> fieldErrors) {
        return new ApiError(status, error, message, path, LocalDateTime.now(), fieldErrors);
    }
}
