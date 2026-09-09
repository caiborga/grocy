package de.skit.grocy.web;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.skit.grocy.security.AuthService;

@RestController
@RequestMapping("/api/public")
public class PublicConfigController {

    private final AuthService authService;

    public PublicConfigController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/config")
    public Map<String, Boolean> config() {
        return Map.of("registrationEnabled", authService.isRegistrationEnabled());
    }
}
