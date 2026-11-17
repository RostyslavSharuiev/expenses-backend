package rostyslav.sharuiev.expenses.authentication.infrastructure.security;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    private JwtTokenValidator jwtTokenValidator;
    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        jwtTokenValidator = mock(JwtTokenValidator.class);
        filter = new JwtAuthenticationFilter(jwtTokenValidator);

        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldAuthenticateUserForValidBearerToken() throws ServletException, IOException {
        UUID userId = UUID.randomUUID();
        String token = "valid-token";

        when(jwtTokenValidator.validateAndGetUserId(token)).thenReturn(Optional.of(userId));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        filter.doFilter(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        assertNotNull(authentication);
        assertTrue(authentication.isAuthenticated());
        assertEquals(userId, authentication.getPrincipal());
        assertNull(authentication.getCredentials());

        verify(jwtTokenValidator).validateAndGetUserId(token);
    }

    @Test
    void shouldNotAuthenticateUserForInvalidBearerToken() throws ServletException, IOException {
        String token = "invalid-token";

        when(jwtTokenValidator.validateAndGetUserId(token)).thenReturn(Optional.empty());

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verify(jwtTokenValidator).validateAndGetUserId(token);
    }

    @Test
    void shouldNotAuthenticateUserWithoutAuthorizationHeader() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verifyNoInteractions(jwtTokenValidator);
    }

    @Test
    void shouldIgnoreAuthorizationHeaderWithDifferentScheme() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic some-credentials");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verifyNoInteractions(jwtTokenValidator);
    }
}
