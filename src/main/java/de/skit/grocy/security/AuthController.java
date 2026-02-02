package de.skit.grocy.security;

import de.skit.grocy.security.dto.LoginRequest;
import de.skit.grocy.security.dto.LoginResponse;
import de.skit.grocy.user.UserEntity;
import de.skit.grocy.user.dto.UserResponse;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email(),
                            request.password()));
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(401).body(new LoginResponse(null, null, null));
        }

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        UserEntity user = principal.getUser();

        String token = jwtService.generateToken(principal);

        UserResponse userResponse = new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getActiveHouseholdId()
            );

        return ResponseEntity.ok(
                new LoginResponse(token, "Bearer", userResponse));
    }
}
