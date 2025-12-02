package com.avialex.api.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;

@Entity
@Table(name = "revoked_jti")
@Data
public class RevokedJti {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private String jti;

	@Column(nullable = false)
	private Instant revokedAt = Instant.now();
}


