package com.openclassrooms.starterjwt.services;

import com.openclassrooms.starterjwt.exception.ForbiddenException;
import com.openclassrooms.starterjwt.configuration.YogaProperties;
import com.openclassrooms.starterjwt.exception.NotFoundException;
import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.repository.UserRepository;

import java.util.Objects;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final YogaProperties yogaProperties;

    public UserService(UserRepository userRepository, YogaProperties yogaProperties) {
        this.userRepository = userRepository;
        this.yogaProperties = yogaProperties;
    }

    public void delete(Long id) {
        User user = findById(id);

        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!Objects.equals(userDetails.getUsername(), user.getEmail())) {
            throw new ForbiddenException(
                    yogaProperties.getMessages().getErrors().getUserDeletionForbidden());
        }
        this.userRepository.deleteById(id);
    }

    public User findById(Long id) {
        return this.userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        yogaProperties.getMessages().getErrors().getUserNotFound().formatted(id)));
    }
}
