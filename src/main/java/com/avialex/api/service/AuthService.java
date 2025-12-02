package com.avialex.api.service;

import com.avialex.api.model.dto.AuthResponseDTO;
import com.avialex.api.model.dto.SignInRequestDTO;
import com.avialex.api.model.dto.SignUpRequestDTO;
import com.avialex.api.model.entity.User;
import com.avialex.api.model.entity.RefreshToken;
import com.avialex.api.model.entity.UserType;
import com.avialex.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import com.avialex.api.config.JwtConfig;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.naming.AuthenticationException;
import java.util.ArrayList;
import java.util.List;
import java.time.Duration;
import java.time.Instant;

@Service
public class AuthService {

    private final JwtConfig jwtConfig;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    @Autowired
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, RefreshTokenService refreshTokenService, JwtConfig jwtConfig) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
        this.jwtConfig = jwtConfig;
    }

    public AuthResponseDTO signin(SignInRequestDTO request) throws Exception {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new Exception("Username/Password not found"));

        boolean passwordMatches = passwordEncoder.matches(request.password(), user.getPassword());
        if (!passwordMatches) {
            throw new AuthenticationException();
        }

        return issueTokensForUser(user);
    }

    public User signup(SignUpRequestDTO req) {
        User user = new User();
        user.setName(req.name());
        user.setAddress(req.address());
        user.setEmail(req.email());
        user.setPhone(req.phone());
        user.setPassword(req.password());
        user.setCpf(req.cpf());
        user.setRg(req.rg());
        user.setType(req.type() == null ? UserType.CLIENT : req.type());
        return userRepository.save(user);
    }

    public AuthResponseDTO refresh(String refreshToken) throws Exception {
        RefreshToken rt = refreshTokenService.validate(refreshToken)
                .orElseThrow(() -> new Exception("Invalid refresh token"));

        User user = rt.getUser();
        return issueTokensForUser(user);
    }

    public void signout(User user, String refreshToken) {
        refreshTokenService.validate(refreshToken).ifPresent(refreshTokenService::revoke);
    }

    public AuthResponseDTO issueTokensForUser(User user) {
        JwtConfig.Key signing = jwtConfig.getPrimaryKeyOrThrow();
        byte[] keyBytes;
        try {
            keyBytes = java.security.MessageDigest.getInstance("SHA-256").digest(signing.getSecret().getBytes(java.nio.charset.StandardCharsets.UTF_8));
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
        Algorithm algorithm = Algorithm.HMAC256(keyBytes);
        Instant now = Instant.now();
        Instant expiresIn = now.plus(Duration.ofMinutes(15));
        String token = JWT.create()
                .withIssuer("avialex")
                .withSubject(user.getId().toString())
                .withClaim("roles", getPlatformRoles(user))
                .withClaim("domains", getDomainRoles(user))
                .withAudience(jwtConfig.getAudience())
                .withIssuedAt(java.util.Date.from(now))
                .withNotBefore(java.util.Date.from(now.minusSeconds(5)))
                .withJWTId(java.util.UUID.randomUUID().toString())
                .withHeader(java.util.Map.of("kid", signing.getId()))
                .withExpiresAt(expiresIn)
                .sign(algorithm);

        RefreshToken refresh = refreshTokenService.mint(user);
        return new AuthResponseDTO(token, expiresIn.toEpochMilli(), refresh.getToken());
    }

    private static List<String> getPlatformRoles(User user) {
        List<String> roles = new ArrayList<>();
        roles.add("USER");
        if (user.getType() == UserType.MANAGER) {
            roles.add("ADMIN");
        }
        if (user.getType() == UserType.MARKETING || user.getType() == UserType.LAWYER || user.getType() == UserType.MANAGER) {
            roles.add("STAFF");
        }
        return roles;
    }

    private static List<String> getDomainRoles(User user) {
        List<String> roles = new ArrayList<>();
        roles.add("CLIENT");
        if (user.getType().equals(UserType.MANAGER)) {
            roles.add("MANAGER");
        }
        if (user.getType().equals(UserType.MARKETING)) {
            roles.add("MARKETING");
        }
        if (user.getType().equals(UserType.LAWYER)) {
            roles.add("LAWYER");
        }
        return roles;
    }
}
