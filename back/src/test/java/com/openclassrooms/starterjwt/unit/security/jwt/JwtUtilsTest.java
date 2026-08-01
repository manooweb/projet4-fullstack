package com.openclassrooms.starterjwt.unit.security.jwt;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.openclassrooms.starterjwt.security.jwt.JwtUtils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@DisplayName("Given that a JWT token is validated")
class JwtUtilsTest {

    private static final String JWT_SECRET = Base64.getEncoder().encodeToString(
            "1234567890123456789012345678901234567890123456789012345678901234"
                    .getBytes(StandardCharsets.UTF_8));
    private static final String OTHER_JWT_SECRET = Base64.getEncoder().encodeToString(
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789+/"
                    .getBytes(StandardCharsets.UTF_8));

    private JwtUtils jwtUtils;
    private SecretKey signingKey;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils(JWT_SECRET, 60_000);
        signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(JWT_SECRET));
    }

    @Test
    @DisplayName("When the token is valid, then it should be accepted")
    void shouldValidateToken() {
        String token = signedToken(signingKey, Instant.now().plusSeconds(60));

        assertTrue(jwtUtils.validateJwtToken(token));
    }

    @Nested
    @DisplayName("Given an invalid token")
    class InvalidTokenTests {

        @Test
        @DisplayName("When its signature is invalid, then it should be rejected")
        void shouldRejectTokenWithInvalidSignature() {
            SecretKey otherSigningKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(OTHER_JWT_SECRET));
            String token = signedToken(otherSigningKey, Instant.now().plusSeconds(60));

            assertFalse(jwtUtils.validateJwtToken(token));
        }

        @Test
        @DisplayName("When its format is malformed, then it should be rejected")
        void shouldRejectMalformedToken() {
            assertFalse(jwtUtils.validateJwtToken("not-a-jwt"));
        }

        @Test
        @DisplayName("When it is expired, then it should be rejected")
        void shouldRejectExpiredToken() {
            String token = signedToken(signingKey, Instant.now().minusSeconds(60));

            assertFalse(jwtUtils.validateJwtToken(token));
        }

        @Test
        @DisplayName("When it is unsigned, then it should be rejected")
        void shouldRejectUnsignedToken() {
            String token = Jwts.builder().subject("demo@example.com").compact();

            assertFalse(jwtUtils.validateJwtToken(token));
        }

        @Test
        @DisplayName("When it is absent, then it should be rejected")
        void shouldRejectAbsentToken() {
            assertFalse(jwtUtils.validateJwtToken(null));
        }
    }

    private String signedToken(SecretKey key, Instant expiration) {
        return Jwts.builder()
                .subject("demo@example.com")
                .expiration(Date.from(expiration))
                .signWith(key, Jwts.SIG.HS512)
                .compact();
    }
}
