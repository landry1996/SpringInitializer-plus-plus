package com.springforge.user.application;

import com.springforge.shared.exception.BusinessException;
import com.springforge.shared.security.JwtService;
import com.springforge.shared.security.LoginAttemptService;
import com.springforge.user.domain.RefreshToken;
import com.springforge.user.domain.RefreshTokenRepository;
import com.springforge.user.domain.User;
import com.springforge.user.domain.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class LoginUseCase {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final LoginAttemptService loginAttemptService;

    public LoginUseCase(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder, JwtService jwtService,
                       LoginAttemptService loginAttemptService) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.loginAttemptService = loginAttemptService;
    }

    @Transactional
    public AuthResponse execute(LoginRequest request) {
        if (loginAttemptService.isBlocked(request.email())) {
            throw new BusinessException("ACCOUNT_LOCKED", "Account temporarily locked due to too many failed attempts");
        }

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    loginAttemptService.loginFailed(request.email());
                    return new BusinessException("INVALID_CREDENTIALS", "Invalid email or password");
                });

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            loginAttemptService.loginFailed(request.email());
            throw new BusinessException("INVALID_CREDENTIALS", "Invalid email or password");
        }

        loginAttemptService.loginSucceeded(request.email());

        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String refreshTokenValue = jwtService.generateRefreshToken(user.getId());

        RefreshToken refreshToken = new RefreshToken(
                refreshTokenValue, user, LocalDateTime.now().plusDays(7));
        refreshTokenRepository.save(refreshToken);

        return new AuthResponse(accessToken, refreshTokenValue, UserResponse.from(user));
    }
}
