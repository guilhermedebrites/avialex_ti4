package com.avialex.api.config;

import com.avialex.api.model.entity.User;
import com.avialex.api.model.entity.UserType;
import com.avialex.api.model.dto.AuthResponseDTO;
import com.avialex.api.repository.UserRepository;
import com.avialex.api.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class GithubOAuth2SuccessHandler implements AuthenticationSuccessHandler {

	private final UserRepository userRepository;
	private final AuthService authService;
	private final ObjectMapper objectMapper = new ObjectMapper();

	public GithubOAuth2SuccessHandler(UserRepository userRepository, AuthService authService) {
		this.userRepository = userRepository;
		this.authService = authService;
	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
		OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
		String email = (String) oauthUser.getAttributes().getOrDefault("email", oauthUser.getAttributes().get("login") + "@users.noreply.github.com");

		User user = userRepository.findByEmail(email).orElseGet(() -> {
			User u = new User();
			u.setEmail(email);
			u.setName((String) oauthUser.getAttributes().getOrDefault("name", "GitHub User"));
			u.setAddress("");
			u.setPhone("");
			u.setPassword(java.util.UUID.randomUUID().toString());
			u.setCpf("");
			u.setRg("");
			u.setType(UserType.CLIENT);
			return userRepository.save(u);
		});

		AuthResponseDTO tokens = authService.issueTokensForUser(user);
		response.setStatus(200);
		response.setContentType("application/json");
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		response.getWriter().write(objectMapper.writeValueAsString(tokens));
	}
}


