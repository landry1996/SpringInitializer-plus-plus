package com.springforge.user.application;

import com.springforge.shared.exception.BusinessException;
import com.springforge.shared.security.JwtService;
import com.springforge.user.domain.RefreshToken;
import com.springforge.user.domain.RefreshTokenRepository;
import com.springforge.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class RefreshTokenUseCase {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    public RefreshTokenUseCase(RefreshTokenRepository refreshTokenRepository, JwtService jwtService) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse execute(RefreshRequest request) {
        RefreshToken existing = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new BusinessException("INVALID_TOKEN", "Invalid refresh token"));

        if (!existing.isValid()) {
            throw new BusinessException("TOKEN_EXPIRED", "Refresh token expired or revoked");
        }

        // Rotation: revoke old, issue new
        existing.revoke();
        refreshTokenRepository.save(existing);

        User user = existing.getUser();
        String newAccessToken = jwtService.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String newRefreshToken = jwtService.generateRefreshToken(user.getId());

        RefreshToken newToken = new RefreshToken(newRefreshToken, user, LocalDateTime.now().plusDays(7));
        refreshTokenRepository.save(newToken);

        return new AuthResponse(newAccessToken, newRefreshToken, UserResponse.from(user));
    }
}
