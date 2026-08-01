package com.openclassrooms.starterjwt.unit.security.jwt;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.openclassrooms.starterjwt.security.handler.ApiAuthenticationEntryPoint;
import com.openclassrooms.starterjwt.security.jwt.AuthTokenFilter;
import com.openclassrooms.starterjwt.security.jwt.JwtUtils;
import com.openclassrooms.starterjwt.security.services.UserDetailsServiceImpl;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

@ExtendWith(MockitoExtension.class)
@DisplayName("Given that an authentication token filter processes a request")
class AuthTokenFilterTest {

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @Mock
    private ApiAuthenticationEntryPoint authenticationEntryPoint;

    @Mock
    private FilterChain filterChain;

    private AuthTokenFilter authTokenFilter;

    @BeforeEach
    void setUp() {
        authTokenFilter = new AuthTokenFilter(jwtUtils, userDetailsService, authenticationEntryPoint);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("When authentication fails, then the context is cleared and the filter chain stops")
    void shouldHandleAuthenticationException() throws ServletException, IOException {
        String token = "valid-token";
        String username = "demo@example.com";
        UsernameNotFoundException exception = new UsernameNotFoundException("User not found");
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("Authorization", "Bearer " + token);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("authenticated-user", null));
        when(jwtUtils.validateJwtToken(token)).thenReturn(true);
        when(jwtUtils.getUserNameFromJwtToken(token)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenThrow(exception);

        authTokenFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(authenticationEntryPoint).commence(request, response, exception);
        verifyNoInteractions(filterChain);
    }

    @Test
    @DisplayName("When the request contains a Bearer token, then it should be validated")
    void shouldValidateBearerToken() throws ServletException, IOException {
        String token = "valid-token";
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("Authorization", "Bearer " + token);
        when(jwtUtils.validateJwtToken(token)).thenReturn(false);

        authTokenFilter.doFilter(request, response, filterChain);

        verify(jwtUtils).validateJwtToken(token);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("When the request has no authorization header, then no token should be validated")
    void shouldNotValidateTokenWithoutAuthorizationHeader() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        authTokenFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(jwtUtils, userDetailsService, authenticationEntryPoint);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("When the authorization header does not use Bearer, then no token should be validated")
    void shouldNotValidateTokenWithoutBearerPrefix() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("Authorization", "Basic credentials");

        authTokenFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(jwtUtils, userDetailsService, authenticationEntryPoint);
        verify(filterChain).doFilter(request, response);
    }
}
