package rostyslav.sharuiev.expenses.authentication.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenValidator jwtTokenValidator;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorizationHeader != null && authorizationHeader.startsWith(BEARER_PREFIX)) {
            String token = authorizationHeader.substring(BEARER_PREFIX.length());

            jwtTokenValidator
                    .validateAndGetUserId(token)
                    .ifPresent(this::authenticate);
        }

        filterChain.doFilter(request, response);

    }

    private void authenticate(UUID userId) {
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            return;
        }
        
        var authentication = new UsernamePasswordAuthenticationToken(
                userId,
                null,
                Collections.emptyList()
        );

        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);
    }
}
