package de.skit.grocy.security;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import de.skit.grocy.security.session.AuthSessionService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    public static final String JTI_ATTRIBUTE = "grocy.jwt.jti";

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final AuthSessionService authSessionService;

    public JwtAuthFilter(
            JwtService jwtService,
            UserDetailsService userDetailsService,
            AuthSessionService authSessionService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.authSessionService = authSessionService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = authHeader.substring(7);
            String username = jwtService.extractUsername(token);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails user = userDetailsService.loadUserByUsername(username);

                if (user.isEnabled() && user.isAccountNonLocked() && jwtService.isTokenValid(token, user)) {
                    String jti = jwtService.extractJti(token);
                    if (jti != null && user instanceof UserPrincipal principal
                            && !authSessionService.isActive(jti, principal.getUser().getId())) {
                        unauthorized(response);
                        return;
                    }
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            user, null, user.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    if (jti != null) {
                        request.setAttribute(JTI_ATTRIBUTE, jti);
                    }
                }
            }

            filterChain.doFilter(request, response);

        } catch (JwtException | UsernameNotFoundException e) {
            SecurityContextHolder.clearContext();
            unauthorized(response);
        }
    }

    private void unauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("Cache-Control", "no-store");
        response.getWriter().write("{\"code\":\"unauthorized\",\"message\":\"Sitzung ungültig oder abgelaufen\"}");
    }
}
