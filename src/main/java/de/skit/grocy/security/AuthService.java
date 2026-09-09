package de.skit.grocy.security;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import de.skit.grocy.mail.MailService;
import de.skit.grocy.security.dto.ForgotPasswordRequest;
import de.skit.grocy.security.dto.RegisterRequest;
import de.skit.grocy.security.dto.RegisterResponse;
import de.skit.grocy.security.dto.ResetPasswordRequest;
import de.skit.grocy.security.dto.VerifyEmailRequest;
import de.skit.grocy.security.session.AuthSessionService;
import de.skit.grocy.security.token.AuthTokenService;
import de.skit.grocy.security.token.AuthTokenType;
import de.skit.grocy.user.UserEntity;
import de.skit.grocy.user.UserRepository;
import de.skit.grocy.user.UserService;
import de.skit.grocy.user.dto.UserCreate;

@Service
public class AuthService {

    private static final Duration EMAIL_VERIFICATION_TTL = Duration.ofHours(24);
    private static final Duration PASSWORD_RESET_TTL = Duration.ofMinutes(30);
    private static final String GENERIC_REGISTER_MESSAGE =
            "Falls die Adresse neu ist, wurde eine Bestätigungs-E-Mail versendet.";

    private final UserService userService;
    private final UserRepository userRepository;
    private final AuthTokenService tokenService;
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;
    private final AuthSessionService authSessionService;
    private final String frontendUrl;
    private final boolean registrationEnabled;
    private final String dummyPasswordHash;

    public AuthService(
            UserService userService,
            UserRepository userRepository,
            AuthTokenService tokenService,
            MailService mailService,
            PasswordEncoder passwordEncoder,
            AuthSessionService authSessionService,
            @Value("${app.frontend-url}") String frontendUrl,
            @Value("${app.registration.enabled:true}") boolean registrationEnabled) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.tokenService = tokenService;
        this.mailService = mailService;
        this.passwordEncoder = passwordEncoder;
        this.authSessionService = authSessionService;
        this.frontendUrl = frontendUrl;
        this.registrationEnabled = registrationEnabled;
        this.dummyPasswordHash = passwordEncoder.encode("grocy-dummy-not-a-real-password");
    }

    public boolean isRegistrationEnabled() {
        return registrationEnabled;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (!registrationEnabled) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Registrierung ist derzeit nicht möglich.");
        }

        String email = normalizeEmail(request.email());
        Optional<UserEntity> existing = userRepository.findByEmail(email)
                .filter(user -> user.getDeletedAt() == null);

        if (existing.isPresent()) {
            passwordEncoder.matches(request.password(), dummyPasswordHash);
            return new RegisterResponse(GENERIC_REGISTER_MESSAGE, email);
        }

        UserEntity user = userService.createUserEntity(
                new UserCreate(request.displayName(), email, request.password()));
        sendVerificationMail(user);
        return new RegisterResponse(GENERIC_REGISTER_MESSAGE, email);
    }

    @Transactional
    public UserEntity login(String email, String password) {
        String normalized = normalizeEmail(email);
        UserEntity user = userRepository.findByEmail(normalized)
                .filter(candidate -> candidate.getDeletedAt() == null)
                .orElse(null);

        if (user == null) {
            passwordEncoder.matches(password, dummyPasswordHash);
            throw new BadCredentialsException("Bad credentials");
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BadCredentialsException("Bad credentials");
        }

        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(OffsetDateTime.now())) {
            throw new LockedException("locked");
        }

        if (!user.isEmailVerified()) {
            throw new DisabledException("email not verified");
        }

        user.setLastLoginAt(OffsetDateTime.now());
        return userRepository.save(user);
    }

    @Transactional
    public void verifyEmail(VerifyEmailRequest request) {
        UserEntity user = tokenService.consumeToken(request.token(), AuthTokenType.EMAIL_VERIFICATION);
        if (user.getDeletedAt() != null) {
            throw new IllegalArgumentException("Token ist ungültig oder abgelaufen");
        }
        user.setEmailVerified(true);
        user.setEmailVerifiedAt(OffsetDateTime.now());
    }

    @Transactional
    public void resendVerification(ForgotPasswordRequest request) {
        userRepository.findByEmail(normalizeEmail(request.email()))
                .filter(user -> user.getDeletedAt() == null)
                .filter(user -> !user.isEmailVerified())
                .ifPresent(this::sendVerificationMail);
    }

    @Transactional
    public void requestPasswordReset(ForgotPasswordRequest request) {
        userRepository.findByEmail(normalizeEmail(request.email()))
                .filter(user -> user.getDeletedAt() == null)
                .filter(UserEntity::isEmailVerified)
                .ifPresent(user -> {
                    String token = tokenService.createToken(user, AuthTokenType.PASSWORD_RESET, PASSWORD_RESET_TTL);
                    mailService.sendPasswordResetMail(user.getEmail(), buildLink("/reset-password", token));
                });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        UserEntity user = tokenService.consumeToken(request.token(), AuthTokenType.PASSWORD_RESET);
        if (user.getDeletedAt() != null) {
            throw new IllegalArgumentException("Token ist ungültig oder abgelaufen");
        }
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setCredentialsValidFrom(OffsetDateTime.now());
        authSessionService.revokeAll(user);
        tokenService.invalidateUnused(user, AuthTokenType.PASSWORD_RESET);
    }

    public void logout(String jti) {
        authSessionService.revokeByJti(jti);
    }

    private void sendVerificationMail(UserEntity user) {
        String token = tokenService.createToken(user, AuthTokenType.EMAIL_VERIFICATION, EMAIL_VERIFICATION_TTL);
        mailService.sendVerificationMail(user.getEmail(), buildLink("/verify-email", token));
    }

    private String buildLink(String path, String token) {
        return UriComponentsBuilder
                .fromUriString(frontendUrl)
                .path(path)
                .queryParam("token", token)
                .build()
                .toUriString();
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}
