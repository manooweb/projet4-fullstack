package com.openclassrooms.starterjwt.unit.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.openclassrooms.starterjwt.configuration.properties.YogaMessagesProperties;
import com.openclassrooms.starterjwt.configuration.properties.YogaProperties;
import com.openclassrooms.starterjwt.exception.BadRequestException;
import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.payload.request.SignupRequest;
import com.openclassrooms.starterjwt.payload.response.JwtResponse;
import com.openclassrooms.starterjwt.payload.response.MessageResponse;
import com.openclassrooms.starterjwt.repository.UserRepository;
import com.openclassrooms.starterjwt.security.jwt.JwtUtils;
import com.openclassrooms.starterjwt.security.services.UserDetailsImpl;
import com.openclassrooms.starterjwt.services.AuthService;

@ExtendWith(MockitoExtension.class)
@DisplayName("Given that an authentication service")
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        YogaMessagesProperties messages = new YogaMessagesProperties();
        messages.getErrors().setEmailAlreadyTaken("Error: Email is already taken!");
        messages.getSuccess().setUserRegistered("User registered successfully!");

        authService = new AuthService(
                authenticationManager,
                jwtUtils,
                userRepository,
                passwordEncoder,
                new YogaProperties(messages));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("Given valid credentials")
    class AuthenticationTests {

        @Test
        @DisplayName("When the user authenticates, then a JWT response should be returned")
        void shouldAuthenticateUser() {
            // Given an authenticated principal returned by Spring Security
            String email = "demo@example.com";
            Authentication authentication = mock(Authentication.class);
            UserDetailsImpl userDetails = UserDetailsImpl.builder()
                    .id(1L)
                    .username(email)
                    .firstName("John")
                    .lastName("Doe")
                    .admin(true)
                    .password("encoded-password")
                    .build();

            when(authenticationManager.authenticate(any())).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(userDetails);
            when(jwtUtils.generateJwtToken(authentication)).thenReturn("generated-jwt");

            // When the user authenticates
            JwtResponse response = authService.authenticateUser(email, "password");

            // Then a JWT response should be returned and authentication stored in the context
            assertEquals("generated-jwt", response.getToken());
            assertEquals("Bearer", response.getType());
            assertEquals(1L, response.getId());
            assertEquals(email, response.getUsername());
            assertEquals("John", response.getFirstName());
            assertEquals("Doe", response.getLastName());
            assertTrue(response.getAdmin());
            assertSame(authentication, SecurityContextHolder.getContext().getAuthentication());
            verify(jwtUtils).generateJwtToken(authentication);
        }
    }

    @Nested
    @DisplayName("Given a user registration request")
    class RegistrationTests {

        @Test
        @DisplayName("When the email is available, then the user should be registered")
        void shouldRegisterUser() {
            // Given an available email and a valid registration request
            SignupRequest signUpRequest = signUpRequest("demo@example.com", "password", "John", "Doe");
            when(userRepository.existsByEmail(signUpRequest.getEmail())).thenReturn(false);
            when(passwordEncoder.encode(signUpRequest.getPassword())).thenReturn("encoded-password");

            // When the user registers
            MessageResponse response = authService.registerUser(signUpRequest);

            // Then the encoded user should be saved and a success message returned
            verify(userRepository).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();
            assertEquals("demo@example.com", savedUser.getEmail());
            assertEquals("John", savedUser.getFirstName());
            assertEquals("Doe", savedUser.getLastName());
            assertEquals("encoded-password", savedUser.getPassword());
            assertFalse(savedUser.isAdmin());
            assertEquals("User registered successfully!", response.getMessage());
        }

        @Test
        @DisplayName("When the email is already taken, then the user should not be registered")
        void shouldNotRegisterUserWithExistingEmail() {
            // Given an existing email and a valid registration request
            SignupRequest signUpRequest = signUpRequest("demo@example.com", "password", "John", "Doe");
            when(userRepository.existsByEmail(signUpRequest.getEmail())).thenReturn(true);

            // When the user registers
            BadRequestException exception = assertThrows(
                    BadRequestException.class,
                    () -> authService.registerUser(signUpRequest));

            // Then the user should not be saved
            assertEquals("Error: Email is already taken!", exception.getMessage());
            verify(userRepository, never()).save(any(User.class));
            verifyNoInteractions(passwordEncoder);
        }
    }

    private SignupRequest signUpRequest(String email, String password, String firstName, String lastName) {
        SignupRequest signUpRequest = new SignupRequest();
        signUpRequest.setEmail(email);
        signUpRequest.setPassword(password);
        signUpRequest.setFirstName(firstName);
        signUpRequest.setLastName(lastName);
        return signUpRequest;
    }
}
