package com.springforge.shared.security;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_TIME_MS = 15 * 60 * 1000; // 15 minutes

    private final Map<String, LoginAttempt> attempts = new ConcurrentHashMap<>();

    public void loginSucceeded(String email) {
        attempts.remove(email);
    }

    public void loginFailed(String email) {
        LoginAttempt attempt = attempts.computeIfAbsent(email, k -> new LoginAttempt());
        attempt.increment();
    }

    public boolean isBlocked(String email) {
        LoginAttempt attempt = attempts.get(email);
        if (attempt == null) return false;
        if (attempt.getCount() < MAX_ATTEMPTS) return false;
        if (System.currentTimeMillis() - attempt.getLastAttempt() > LOCK_TIME_MS) {
            attempts.remove(email);
            return false;
        }
        return true;
    }

    private static class LoginAttempt {
        private int count;
        private long lastAttempt;

        void increment() {
            count++;
            lastAttempt = System.currentTimeMillis();
        }

        int getCount() { return count; }
        long getLastAttempt() { return lastAttempt; }
    }
}
