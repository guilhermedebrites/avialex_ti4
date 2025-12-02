package com.avialex.api.repository;

import com.avialex.api.model.entity.RevokedJti;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RevokedJtiRepository extends JpaRepository<RevokedJti, Long> {
	boolean existsByJti(String jti);
}


