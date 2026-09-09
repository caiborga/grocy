package de.skit.grocy.security.ratelimit;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Resolves the client IP for rate limiting. Forwarded headers are used only when
 * the immediate peer is a configured trusted proxy (default: loopback). Direct
 * clients cannot spoof {@code X-Forwarded-For}.
 */
@Component
public class ClientIpResolver {

    private final Set<String> trustedProxies;

    public ClientIpResolver(
            @Value("${app.rate-limit.trusted-proxies:127.0.0.1,::1}") String trustedProxies) {
        this.trustedProxies = Arrays.stream(trustedProxies.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .map(ClientIpResolver::normalizeLiteralIp)
                .filter(value -> value != null)
                .collect(Collectors.toUnmodifiableSet());
    }

    public String resolve(HttpServletRequest request) {
        String remote = normalizeLiteralIp(request.getRemoteAddr());
        if (remote == null) {
            return "unknown";
        }
        if (!trustedProxies.contains(remote)) {
            return remote;
        }
        String realIp = firstLiteralIp(request.getHeader("X-Real-IP"));
        if (realIp != null) {
            return realIp;
        }
        String forwarded = rightmostUntrusted(request.getHeader("X-Forwarded-For"));
        if (forwarded != null) {
            return forwarded;
        }
        return remote;
    }

    private String rightmostUntrusted(String header) {
        if (header == null || header.isBlank()) {
            return null;
        }
        String[] parts = header.split(",");
        for (int i = parts.length - 1; i >= 0; i--) {
            String candidate = normalizeLiteralIp(parts[i]);
            if (candidate != null && !trustedProxies.contains(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private static String firstLiteralIp(String header) {
        if (header == null || header.isBlank()) {
            return null;
        }
        return normalizeLiteralIp(header.split(",")[0]);
    }

    static String normalizeLiteralIp(String raw) {
        if (raw == null) {
            return null;
        }
        String value = raw.trim();
        if (value.length() >= 2 && value.startsWith("[") && value.endsWith("]")) {
            value = value.substring(1, value.length() - 1);
        }
        int zone = value.indexOf('%');
        if (zone >= 0) {
            value = value.substring(0, zone);
        }
        if (!isLiteralIp(value)) {
            return null;
        }
        try {
            InetAddress address = InetAddress.getByName(value);
            byte[] bytes = address.getAddress();
            if (bytes.length == 16 && isIpv4Mapped(bytes)) {
                return (bytes[12] & 0xff) + "." + (bytes[13] & 0xff) + "."
                        + (bytes[14] & 0xff) + "." + (bytes[15] & 0xff);
            }
            if (address.isLoopbackAddress()) {
                return address instanceof java.net.Inet6Address ? "::1" : "127.0.0.1";
            }
            return address.getHostAddress();
        } catch (UnknownHostException ex) {
            return null;
        }
    }

    private static boolean isIpv4Mapped(byte[] bytes) {
        for (int i = 0; i < 10; i++) {
            if (bytes[i] != 0) {
                return false;
            }
        }
        return bytes[10] == (byte) 0xff && bytes[11] == (byte) 0xff;
    }

    private static boolean isLiteralIp(String value) {
        if (value.isEmpty() || value.length() > 45) {
            return false;
        }
        String lower = value.toLowerCase(Locale.ROOT);
        if (lower.chars().allMatch(c -> (c >= '0' && c <= '9') || c == '.')) {
            return true;
        }
        if (!lower.contains(":")) {
            return false;
        }
        return lower.chars().allMatch(c -> (c >= '0' && c <= '9')
                || (c >= 'a' && c <= 'f')
                || c == ':'
                || c == '.');
    }
}
