package com.openclassrooms.starterjwt.integration.support;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.repository.UserRepository;

public class UserTestHelper {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserTestHelper(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User createTestUser(String email, String rawPassword) {
        return createTestUser(email, rawPassword, false);
    }

    public User createTestUser(String email, String rawPassword, boolean admin) {
        User user = new User(
                email,
                "Demo",
                "Test",
                passwordEncoder.encode(rawPassword),
                admin);

        return userRepository.save(user);
    }
}
