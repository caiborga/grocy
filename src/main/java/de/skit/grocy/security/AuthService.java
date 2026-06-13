package de.skit.grocy.security;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import de.skit.grocy.mail.MailService;
import de.skit.grocy.security.dto.ForgotPasswordRequest;
import de.skit.grocy.security.dto.RegisterRequest;
import de.skit.grocy.security.dto.RegisterResponse;
import de.skit.grocy.security.dto.ResetPasswordRequest;
import de.skit.grocy.security.dto.VerifyEmailRequest;
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

    private final UserService userService;
    private final UserRepository userRepository;
    private final AuthTokenService tokenService;
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;
    private final String frontendUrl;

    public AuthService(
            UserService userService,
            UserRepository userRepository,
            AuthTokenService tokenService,
            MailService mailService,
            PasswordEncoder passwordEncoder,
            @Value("${app.frontend-url}") String frontendUrl) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.tokenService = tokenService;
        this.mailService = mailService;
        this.passwordEncoder = passwordEncoder;
        this.frontendUrl = frontendUrl;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        UserEntity user = userService.createUserEntity(
                new UserCreate(request.displayName(), email, request.password()));

        sendVerificationMail(user);

        return new RegisterResponse("Bitte prüfe deine E-Mails und bestätige deinen Account.", user.getEmail());
    }

    @Transactional
    public void verifyEmail(VerifyEmailRequest request) {
        UserEntity user = tokenService.consumeToken(request.token(), AuthTokenType.EMAIL_VERIFICATION);
        user.setEmailVerified(true);
        user.setEmailVerifiedAt(OffsetDateTime.now());
    }

    @Transactional
    public void resendVerification(ForgotPasswordRequest request) {
        Optional<UserEntity> user = userRepository.findByEmail(normalizeEmail(request.email()));
        user.filter(u -> !u.isEmailVerified()).ifPresent(this::sendVerificationMail);
    }

    @Transactional
    public void requestPasswordReset(ForgotPasswordRequest request) {
        userRepository.findByEmail(normalizeEmail(request.email()))
                .filter(UserEntity::isEmailVerified)
                .ifPresent(user -> {
                    String token = tokenService.createToken(user, AuthTokenType.PASSWORD_RESET, PASSWORD_RESET_TTL);
                    mailService.sendPasswordResetMail(user.getEmail(), buildLink("/reset-password", token));
                });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        UserEntity user = tokenService.consumeToken(request.token(), AuthTokenType.PASSWORD_RESET);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
    }

    @Transactional
    public void recordSuccessfulLogin(UserEntity user) {
        user.setLastLoginAt(OffsetDateTime.now());
        userRepository.save(user);
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
