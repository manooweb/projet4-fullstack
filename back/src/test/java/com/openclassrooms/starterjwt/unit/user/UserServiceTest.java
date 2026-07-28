package com.openclassrooms.starterjwt.unit.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import com.openclassrooms.starterjwt.configuration.properties.YogaMessagesProperties;
import com.openclassrooms.starterjwt.configuration.properties.YogaProperties;
import com.openclassrooms.starterjwt.exception.ForbiddenException;
import com.openclassrooms.starterjwt.exception.NotFoundException;
import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.repository.UserRepository;
import com.openclassrooms.starterjwt.services.UserService;

@ExtendWith(MockitoExtension.class)
@DisplayName("Given that a user service")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    private YogaProperties yogaProperties;
    private UserService userService;

    @BeforeEach
    void setUp() {
        YogaMessagesProperties messages = new YogaMessagesProperties();
        messages.getErrors().setUserNotFound("User with id %d was not found.");
        messages.getErrors().setUserDeletionForbidden("You are not allowed to delete this user.");

        yogaProperties = new YogaProperties(messages);
        userService = new UserService(userRepository, yogaProperties);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("Given that an authenticated user")
    class AuthenticatedUser {

        @DisplayName("When deleting their own account, then the user is deleted")
        @Test
        void shouldDeleteUserWhenAuthenticated() {
            Long userId = 1L;

            User userToDelete = new User()
                    .setId(userId)
                    .setEmail("demo@example.com");
            when(userRepository.findById(userId)).thenReturn(Optional.of(userToDelete));

            authenticateUserAs("demo@example.com");

            userService.delete(userId);

            verify(userRepository, times(1)).deleteById(userId);
        }
    }

    @Nested
    @DisplayName("Given that an authenticated user")
    class AnotherAuthenticatedUser {

        @DisplayName("When deleting another user's account, then the user's account can't be deleted")
        @Test
        void canNotDeleteAnotherUserWhenAuthenticated() {
            Long userId = 1L;

            User userToDelete = new User()
                    .setId(userId)
                    .setEmail("another@example.com");
            when(userRepository.findById(userId)).thenReturn(Optional.of(userToDelete));

            authenticateUserAs("demo@example.com");

            ForbiddenException exception = assertThrows(ForbiddenException.class,
                    () -> userService.delete(1L));
            assertThat(exception.getMessage()).isEqualTo("You are not allowed to delete this user.");

            verify(userRepository, never()).deleteById(userId);
        }
    }

    @Nested
    @DisplayName("Given that an existing user")
    class ExistingUser {

        @DisplayName("When requested by id, then the user is returned")
        @Test
        void shouldReturnUserWhenFound() {
            Long userId = 1L;

            User user = new User()
                    .setId(userId)
                    .setFirstName("John")
                    .setLastName("Doe");
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            User actualUser = userService.findById(userId);

            assertThat(actualUser).isEqualTo(user);
            verify(userRepository, times(1)).findById(userId);
        }
    }

    @Nested
    @DisplayName("Given that an unknown user")
    class UnknownUser {

        @DisplayName("When requested by id, then it throws a NotFoundException")
        @Test
        void shouldThrowNotFoundException() {
            Long unknownUserId = 99L;

            when(userRepository.findById(unknownUserId)).thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> userService.findById(unknownUserId));

            assertThat(exception.getMessage()).isEqualTo("User with id %d was not found.".formatted(unknownUserId));

            verify(userRepository, times(1)).findById(unknownUserId);
        }
    }

    private void authenticateUserAs(String email) {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn(email);

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null);

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
