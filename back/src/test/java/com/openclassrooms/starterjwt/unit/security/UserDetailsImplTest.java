package com.openclassrooms.starterjwt.unit.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.openclassrooms.starterjwt.security.services.UserDetailsImpl;

@DisplayName("Given that user details are compared")
class UserDetailsImplTest {

    @Nested
    @DisplayName("Given the equals method")
    class EqualsTests {

        @Test
        @DisplayName("When compared with the same instance, then the details should be equal")
        void shouldBeEqualToSameInstance() {
            UserDetailsImpl expectedUserDetails = userDetails(1L);
            UserDetailsImpl actualUserDetails = expectedUserDetails;

            assertEquals(expectedUserDetails, actualUserDetails);
        }

        @Test
        @DisplayName("When compared with null, then the details should not be equal")
        void shouldNotBeEqualToNull() {
            boolean areEqual = userDetails(1L).equals(null);

            assertFalse(areEqual);
        }

        @Test
        @DisplayName("When compared with another type, then the details should not be equal")
        void shouldNotBeEqualToAnotherType() {
            boolean areEqual = userDetails(1L).equals("user details");

            assertFalse(areEqual);
        }

        @Test
        @DisplayName("When compared with the same ID, then the details should be equal")
        void shouldBeEqualWhenIdsMatch() {
            UserDetailsImpl expectedUserDetails = userDetails(1L);
            UserDetailsImpl actualUserDetails = userDetails(1L);

            assertEquals(expectedUserDetails, actualUserDetails);
        }

        @Test
        @DisplayName("When compared with another ID, then the details should not be equal")
        void shouldNotBeEqualWhenIdsDiffer() {
            assertNotEquals(userDetails(1L), userDetails(2L));
        }
    }

    @Test
    @DisplayName("When Spring Security reads account details, then the account should be active without authorities")
    void shouldExposeActiveAccountWithoutAuthorities() {
        UserDetailsImpl userDetails = userDetails(1L);

        assertTrue(userDetails.getAuthorities().isEmpty());
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());
        assertTrue(userDetails.isEnabled());
    }

    private UserDetailsImpl userDetails(Long id) {
        return UserDetailsImpl.builder()
                .id(id)
                .username("demo@example.com")
                .firstName("John")
                .lastName("Doe")
                .admin(false)
                .password("encoded-password")
                .build();
    }
}
