package com.springforge.user.application;

public record AuthResponse(String accessToken, String refreshToken, UserResponse user) {}
