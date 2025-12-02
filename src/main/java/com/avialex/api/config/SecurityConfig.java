package com.avialex.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.context.annotation.Lazy;
import com.avialex.api.repository.UserRepository;
import com.avialex.api.service.AuthService;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtConfig jwtConfig;
    private final CorsConfigurationSource corsConfigurationSource;

    private final UserRepository userRepository;
    private final AuthService authService;
    private final boolean oauthClientAvailable;

    public SecurityConfig(@Lazy UserRepository userRepository, @Lazy AuthService authService, JwtConfig jwtConfig, CorsConfigurationSource corsConfigurationSource, ObjectProvider<ClientRegistrationRepository> clientRegistrationRepositoryProvider) {
        this.userRepository = userRepository;
        this.authService = authService;
        this.jwtConfig = jwtConfig;
        this.corsConfigurationSource = corsConfigurationSource;
        this.oauthClientAvailable = clientRegistrationRepositoryProvider.getIfAvailable() != null;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/login", "/oauth2/**", "/public/**", "/auth/**", "/process/number/**", "/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/user/**").authenticated()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .decoder(jwtDecoder())
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            )
//            .oauth2Login(oauth2 -> { if (oauthClientAvailable) { oauth2.successHandler(new GithubOAuth2SuccessHandler(userRepository, authService)); } })
            .logout(logout -> logout.logoutUrl("/auth/signout").logoutSuccessHandler((req, res, auth) -> res.setStatus(200)));
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");
        grantedAuthoritiesConverter.setAuthoritiesClaimName("roles");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return token -> {
            try {
                String[] parts = token.split("\\.");
                if (parts.length >= 2) {
                    String headerJson = new String(java.util.Base64.getUrlDecoder().decode(parts[0]));
                    com.fasterxml.jackson.databind.JsonNode node = new com.fasterxml.jackson.databind.ObjectMapper().readTree(headerJson);
                    String kid = node.has("kid") && !node.get("kid").isNull() ? node.get("kid").asText() : null;
                    if (kid != null) {
                        for (JwtConfig.Key k : jwtConfig.getEffectiveKeys()) {
                            if (kid.equals(k.getId())) {
                                byte[] keyBytes = java.security.MessageDigest.getInstance("SHA-256").digest(k.getSecret().getBytes(java.nio.charset.StandardCharsets.UTF_8));
                                javax.crypto.spec.SecretKeySpec spec = new javax.crypto.spec.SecretKeySpec(keyBytes, "HmacSHA256");
                                NimbusJwtDecoder dec = NimbusJwtDecoder.withSecretKey(spec).build();
                                OAuth2TokenValidator<Jwt> withClock = new DelegatingOAuth2TokenValidator<>(
                                        JwtValidators.createDefaultWithIssuer("avialex"),
                                        new JwtTimestampValidator(java.time.Duration.ofSeconds(jwtConfig.getClockSkewSeconds()))
                                );
                                dec.setJwtValidator(withClock);
                                return dec.decode(token);
                            }
                        }
                    }
                }
            } catch (Exception ex) {
               throw new JwtException(ex.getMessage(), ex);
            }
            throw new JwtException("invalid jwt");
        };
    }
}


