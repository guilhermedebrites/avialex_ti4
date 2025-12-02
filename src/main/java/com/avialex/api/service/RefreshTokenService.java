package com.avialex.api.service;

import com.avialex.api.model.entity.RefreshToken;
import com.avialex.api.model.entity.User;
import com.avialex.api.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

	private final RefreshTokenRepository refreshTokenRepository;

	@Value("${security.refresh.minutes:43200}") // default 30 days
	private long refreshMinutes;

	public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
		this.refreshTokenRepository = refreshTokenRepository;
	}

    public RefreshToken mint(User user) {
		RefreshToken rt = new RefreshToken();
		rt.setUser(user);
		rt.setToken(UUID.randomUUID().toString());
		rt.setExpiresAt(Instant.now().plus(Duration.ofMinutes(refreshMinutes)));
		rt.setUserAgent("");
		rt.setIpAddress("");
		return refreshTokenRepository.save(rt);
	}

	public Optional<RefreshToken> validate(String token) {
		return refreshTokenRepository.findByTokenAndRevokedFalse(token)
			.filter(rt -> rt.getExpiresAt().isAfter(Instant.now()));
	}

	public void revoke(RefreshToken token) {
		token.setRevoked(true);
		refreshTokenRepository.save(token);
	}

	public void revokeAll(User user) {
		refreshTokenRepository.deleteAllByUser(user);
	}
}


