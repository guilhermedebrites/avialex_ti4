package com.avialex.api.service;

import com.avialex.api.model.entity.PasswordResetToken;
import com.avialex.api.model.entity.User;
import com.avialex.api.repository.PasswordResetTokenRepository;
import com.avialex.api.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

@Service
public class PasswordResetService {

    private static final Duration DEFAULT_EXPIRATION = Duration.ofMinutes(30);

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;

    public PasswordResetService(PasswordResetTokenRepository tokenRepository, UserRepository userRepository) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
    }

    public String issueResetTokenForEmail(String email) throws Exception {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new Exception("User not found"));
        String token = generateSecureToken();

        PasswordResetToken prt = new PasswordResetToken();
        prt.setUser(user);
        prt.setToken(token);
        prt.setExpiresAt(Instant.now().plus(DEFAULT_EXPIRATION));
        prt.setUsed(false);
        tokenRepository.save(prt);

        return token;
    }

    public Optional<User> validateTokenAndGetUser(String token) {
        return tokenRepository.findByToken(token)
                .filter(t -> !t.isUsed())
                .filter(t -> t.getExpiresAt().isAfter(Instant.now()))
                .map(PasswordResetToken::getUser);
    }

    public void markTokenUsed(String token) {
        tokenRepository.findByToken(token).ifPresent(t -> {
            t.setUsed(true);
            tokenRepository.save(t);
        });
    }

    public boolean resetPassword(String token, String newPassword) {
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token)
                .filter(t -> !t.isUsed())
                .filter(t -> t.getExpiresAt().isAfter(Instant.now()));

        if (tokenOpt.isEmpty()) return false;

        PasswordResetToken prt = tokenOpt.get();
        User user = prt.getUser();
        user.setPassword(newPassword);
        userRepository.save(user);
        prt.setUsed(true);
        tokenRepository.save(prt);
        return true;
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}


