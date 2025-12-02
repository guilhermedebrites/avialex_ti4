package com.avialex.api.repository;

import com.avialex.api.model.entity.RefreshToken;
import com.avialex.api.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
	Optional<RefreshToken> findByTokenAndRevokedFalse(String token);
	void deleteAllByUser(User user);
}


