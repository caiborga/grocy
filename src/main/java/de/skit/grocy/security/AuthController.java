package de.skit.grocy.security;

import de.skit.grocy.security.dto.LoginRequest;
import de.skit.grocy.security.dto.LoginResponse;
import de.skit.grocy.security.dto.AuthMessageResponse;
import de.skit.grocy.security.dto.ForgotPasswordRequest;
import de.skit.grocy.security.dto.RegisterRequest;
import de.skit.grocy.security.dto.RegisterResponse;
import de.skit.grocy.security.dto.ResetPasswordRequest;
import de.skit.grocy.security.dto.VerifyEmailRequest;
import de.skit.grocy.security.session.AuthSessionService;
import de.skit.grocy.user.UserEntity;
import de.skit.grocy.user.dto.UserResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final JwtService jwtService;
    private final AuthService authService;
    private final AuthSessionService authSessionService;

    public AuthController(
            JwtService jwtService,
            AuthService authService,
            AuthSessionService authSessionService) {
        this.jwtService = jwtService;
        this.authService = authService;
        this.authSessionService = authSessionService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            UserEntity user = authService.login(request.email(), request.password());
            UserPrincipal principal = new UserPrincipal(user);
            JwtService.IssuedJwt issued = jwtService.generateToken(principal);
            authSessionService.create(user, issued.jti(), issued.expiresAt());

            UserResponse userResponse = new UserResponse(
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    user.getCreatedAt(),
                    user.getUpdatedAt(),
                    user.getActiveHouseholdId());

            return ResponseEntity.ok(new LoginResponse(issued.token(), "Bearer", userResponse));
        } catch (DisabledException ex) {
            return ResponseEntity.status(403).body(new LoginResponse(null, "email_not_verified", null));
        } catch (LockedException ex) {
            return ResponseEntity.status(401).body(new LoginResponse(null, null, null));
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(401).body(new LoginResponse(null, null, null));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<AuthMessageResponse> logout(HttpServletRequest request) {
        Object jti = request.getAttribute(JwtAuthFilter.JTI_ATTRIBUTE);
        if (jti instanceof String value) {
            authService.logout(value);
        }
        return ResponseEntity.ok(new AuthMessageResponse("Abgemeldet."));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<AuthMessageResponse> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        authService.verifyEmail(request);
        return ResponseEntity.ok(new AuthMessageResponse("E-Mail wurde bestätigt."));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<AuthMessageResponse> resendVerification(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.resendVerification(request);
        return ResponseEntity.ok(new AuthMessageResponse("Falls der Account existiert, wurde eine E-Mail versendet."));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<AuthMessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.requestPasswordReset(request);
        return ResponseEntity.ok(new AuthMessageResponse("Falls der Account existiert, wurde eine E-Mail versendet."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<AuthMessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(new AuthMessageResponse("Passwort wurde geändert."));
    }
}
