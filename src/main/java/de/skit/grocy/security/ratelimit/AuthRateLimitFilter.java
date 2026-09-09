package de.skit.grocy.security.ratelimit;

import java.time.Duration;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@Component
public class AuthRateLimitFilter extends OncePerRequestFilter {

    private final boolean enabled;
    private final Map<String, ConcurrentLinkedDeque<Long>> windows = new ConcurrentHashMap<>();

    public AuthRateLimitFilter(@Value("${app.rate-limit.enabled:true}") boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        if (!enabled) {
            return true;
        }
        return resolveLimit(request) == null;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        Limit limit = resolveLimit(request);
        if (limit == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = request.getRemoteAddr() + "|" + limit.bucket();
        long now = System.currentTimeMillis();
        long cutoff = now - limit.window().toMillis();

        ConcurrentLinkedDeque<Long> stamps = windows.computeIfAbsent(key, k -> new ConcurrentLinkedDeque<>());
        synchronized (stamps) {
            while (!stamps.isEmpty() && stamps.peekFirst() < cutoff) {
                stamps.pollFirst();
            }
            if (stamps.size() >= limit.maxRequests()) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                response.setHeader("Retry-After", String.valueOf(limit.window().toSeconds()));
                response.setHeader("Cache-Control", "no-store");
                response.getWriter().write("{\"code\":\"rate_limited\",\"message\":\"Zu viele Versuche. Bitte später erneut versuchen.\"}");
                return;
            }
            stamps.addLast(now);
        }

        pruneEmptyWindows();
        filterChain.doFilter(request, response);
    }

    private Limit resolveLimit(HttpServletRequest request) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return null;
        }
        String path = request.getRequestURI();
        if (path == null) {
            return null;
        }
        if (path.equals("/api/auth/login")) {
            return new Limit("login", 10, Duration.ofMinutes(10));
        }
        if (path.equals("/api/auth/register")) {
            return new Limit("register", 5, Duration.ofMinutes(15));
        }
        if (path.equals("/api/auth/forgot-password") || path.equals("/api/auth/resend-verification")) {
            return new Limit("mail", 5, Duration.ofMinutes(15));
        }
        if (path.equals("/api/auth/reset-password") || path.equals("/api/auth/verify-email")) {
            return new Limit("token", 10, Duration.ofMinutes(10));
        }
        if (path.startsWith("/api/invites/") && path.endsWith("/accept")) {
            return new Limit("invite-accept", 20, Duration.ofMinutes(10));
        }
        return null;
    }

    private void pruneEmptyWindows() {
        if (windows.size() < 10_000) {
            return;
        }
        Iterator<Map.Entry<String, ConcurrentLinkedDeque<Long>>> it = windows.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, ConcurrentLinkedDeque<Long>> entry = it.next();
            if (entry.getValue().isEmpty()) {
                it.remove();
            }
        }
    }

    private record Limit(String bucket, int maxRequests, Duration window) {}
}
