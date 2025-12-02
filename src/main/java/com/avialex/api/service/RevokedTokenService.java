package com.avialex.api.service;

import com.avialex.api.model.entity.RevokedJti;
import com.avialex.api.repository.RevokedJtiRepository;
import org.springframework.stereotype.Service;

@Service
public class RevokedTokenService {
	private final RevokedJtiRepository repository;

	public RevokedTokenService(RevokedJtiRepository repository) {
		this.repository = repository;
	}

	public boolean isRevoked(String jti) {
		return repository.existsByJti(jti);
	}

	public void revoke(String jti) {
		if (!repository.existsByJti(jti)) {
			RevokedJti r = new RevokedJti();
			r.setJti(jti);
			repository.save(r);
		}
	}
}


