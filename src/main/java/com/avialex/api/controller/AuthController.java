package com.avialex.api.controller;

import com.avialex.api.model.dto.AuthResponseDTO;
import com.avialex.api.model.dto.ForgotPasswordRequestDTO;
import com.avialex.api.model.dto.ResetPasswordRequestDTO;
import com.avialex.api.model.dto.SignInRequestDTO;
import com.avialex.api.model.dto.SignUpRequestDTO;
import com.avialex.api.model.dto.RefreshTokenRequestDTO;
import com.avialex.api.model.dto.RefreshRequestDTO;
import com.avialex.api.model.entity.User;
import com.avialex.api.service.AuthService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final com.avialex.api.service.PasswordResetService passwordResetService;
    private final com.avialex.api.service.MailService mailService;

    public AuthController(AuthService authService, com.avialex.api.service.PasswordResetService passwordResetService, com.avialex.api.service.MailService mailService) {
        this.authService = authService;
        this.passwordResetService = passwordResetService;
        this.mailService = mailService;
    }

    @PostMapping("/signin")
    public ResponseEntity<?> signin(@RequestBody SignInRequestDTO request){
        try{
            AuthResponseDTO token = authService.signin(request);
            return ResponseEntity.ok(token);
        }catch(Exception e){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<User> signup(@RequestBody SignUpRequestDTO request) {
        User created = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/signout")
    public ResponseEntity<Void> signout() {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/oauth2/google")
    public void redirectToGoogleOAuth(HttpServletResponse response) throws IOException {
        response.sendRedirect("/oauth2/authorization/google");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequestDTO request) {
        try {
            String token = passwordResetService.issueResetTokenForEmail(request.email());
            String resetUrl = String.format("%s/reset-password?token=%s", System.getenv().getOrDefault("APP_PUBLIC_URL", "http://localhost:3000"), token);
            try {
                mailService.send(request.email(), "Redefinição de senha", "Use o link para redefinir sua senha: " + resetUrl);
            } catch (Exception ignored) {}
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            String msg = String.valueOf(e.getMessage());
            if (msg != null && msg.toLowerCase().contains("user not found")) {
                // Retornando status 200 para evitar enumeração
                return ResponseEntity.ok().build();
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unable to process request");
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequestDTO request) {
        boolean ok = passwordResetService.resetPassword(request.token(), request.newPassword());
        if (!ok) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired token");
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshTokenRequestDTO request) {
        try {
            AuthResponseDTO response = authService.refresh(request.refreshToken());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @PostMapping("/signout/revoke")
    public ResponseEntity<Void> signoutRevoke(@RequestBody RefreshTokenRequestDTO request, @AuthenticationPrincipal Jwt jwt) {
        if (jwt != null) {
            // user id is subject
            Long userId = Long.valueOf(jwt.getSubject());
            User u = new User();
            u.setId(userId);
            authService.signout(u, request.refreshToken());
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/sessions")
    public ResponseEntity<?> listSessions() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @PostMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(jwt.getClaims());
    }
}
