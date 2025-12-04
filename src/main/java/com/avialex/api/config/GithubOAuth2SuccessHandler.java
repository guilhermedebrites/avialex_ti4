package com.avialex.api.config;

import com.avialex.api.model.entity.User;
import com.avialex.api.model.entity.UserType;
import com.avialex.api.model.dto.AuthResponseDTO;
import com.avialex.api.repository.UserRepository;
import com.avialex.api.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.core.env.Environment;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class GithubOAuth2SuccessHandler implements AuthenticationSuccessHandler {

	private final UserRepository userRepository;
	private final AuthService authService;
	private final ObjectMapper objectMapper = new ObjectMapper();
	private final Environment environment;

	public GithubOAuth2SuccessHandler(UserRepository userRepository, AuthService authService, Environment environment) {
		this.userRepository = userRepository;
		this.authService = authService;
		this.environment = environment;
	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
		OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
		String registrationId = resolveRegistrationId(authentication);
		String email = resolveEmail(oauthUser, registrationId);

		User user = userRepository.findByEmail(email).orElseGet(() -> userRepository.save(buildUser(email, resolveDisplayName(oauthUser, registrationId))));

		AuthResponseDTO tokens = authService.issueTokensForUser(user);
		
		String frontendUrl = environment.getProperty("app.public-url", "http://localhost:3000");
		
		String tokenJson = objectMapper.writeValueAsString(tokens);
		String encodedTokens = java.net.URLEncoder.encode(tokenJson, StandardCharsets.UTF_8);
		
		String redirectUrl = frontendUrl + "/auth/callback#tokens=" + encodedTokens;
		response.sendRedirect(redirectUrl);
	}

	private static String resolveRegistrationId(Authentication authentication) {
		if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
			return oauthToken.getAuthorizedClientRegistrationId();
		}
		return "unknown";
	}

	private static String resolveEmail(OAuth2User oauthUser, String registrationId) {
		Object emailAttr = oauthUser.getAttributes().get("email");
		if (emailAttr instanceof String email && !email.isBlank()) {
			return email;
		}
		Object loginAttr = oauthUser.getAttributes().get("login");
		if (loginAttr instanceof String login && !login.isBlank()) {
			return login + "@users.noreply.github.com";
		}
		return registrationId + "-" + oauthUser.getName() + "@oauth.local";
	}

	private static String resolveDisplayName(OAuth2User oauthUser, String registrationId) {
		Object nameAttr = oauthUser.getAttributes().get("name");
		if (nameAttr instanceof String name && !name.isBlank()) {
			return name;
		}
		if ("google".equalsIgnoreCase(registrationId)) {
			String given = stringAttr(oauthUser, "given_name");
			String family = stringAttr(oauthUser, "family_name");
			String combined = (given + " " + family).trim();
			if (!combined.isBlank()) {
				return combined;
			}
		}
		Object loginAttr = oauthUser.getAttributes().get("login");
		if (loginAttr instanceof String login && !login.isBlank()) {
			return login;
		}
		return "OAuth User";
	}

	private static String stringAttr(OAuth2User oauthUser, String key) {
		Object attr = oauthUser.getAttributes().get(key);
		return attr instanceof String str ? str : "";
	}

	private static User buildUser(String email, String displayName) {
		User u = new User();
		u.setEmail(email);
		u.setName(displayName);
		u.setAddress("");
		u.setPhone("");
		u.setPassword(java.util.UUID.randomUUID().toString());
		u.setCpf("");
		u.setRg("");
		u.setType(UserType.CLIENT);
		return u;
	}
}


