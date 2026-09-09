package de.skit.grocy.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import de.skit.grocy.user.UserEntity;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PlatformAdminService {

    private final Set<String> adminEmails;

    public PlatformAdminService(@Value("${app.admin.emails:}") String adminEmails) {
        this.adminEmails = Arrays.stream(adminEmails.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .map(value -> value.toLowerCase(Locale.ROOT))
                .collect(Collectors.toUnmodifiableSet());
    }

    public boolean isAdmin(UserEntity user) {
        if (user == null || user.getEmail() == null || user.getDeletedAt() != null) {
            return false;
        }
        return adminEmails.contains(user.getEmail().toLowerCase(Locale.ROOT));
    }

    public void requireAdmin(UserEntity user) {
        if (!isAdmin(user)) {
            throw new org.springframework.security.access.AccessDeniedException("Keine Berechtigung");
        }
    }
}
