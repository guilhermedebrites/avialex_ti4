package com.avialex.api.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "security.token")
public class JwtConfig {
	private List<Key> keys;
	private String audience;
	private long clockSkewSeconds = 60;

	@Value("${security.token.secret.user:}")
	private String legacySecret;

	@Setter
    @Getter
    public static class Key {
		private String id;
		private String secret;

    }

	@PostConstruct
	public void init() {
		if (this.keys == null) {
			this.keys = new ArrayList<>();
		}
		if (this.keys.isEmpty() && legacySecret != null && !legacySecret.isBlank()) {
			Key k = new Key();
			k.setId("legacy");
			k.setSecret(legacySecret);
			this.keys.add(k);
		}
		if (this.audience == null || this.audience.isBlank()) {
			this.audience = "avialex";
		}
	}

	public List<Key> getEffectiveKeys() {
		return keys == null ? Collections.emptyList() : keys;
	}

	public Key getPrimaryKeyOrThrow() {
		List<Key> list = getEffectiveKeys();
		if (list.isEmpty()) {
			throw new IllegalStateException("No signing keys configured. Set security.token.keys[0].secret or security.token.secret.user");
		}
		return list.get(0);
	}
}


