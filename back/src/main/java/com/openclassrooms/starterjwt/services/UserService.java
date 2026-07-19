package com.openclassrooms.starterjwt.services;

import com.openclassrooms.starterjwt.exception.ForbiddenException;
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

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void delete(Long id) {
        User user = findById(id);

        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!Objects.equals(userDetails.getUsername(), user.getEmail())) {
            throw new ForbiddenException(
                    "You are not allowed to delete this user.");
        }
        this.userRepository.deleteById(id);
    }

    public User findById(Long id) {
        return this.userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User with id %d was not found.".formatted(id)));
    }
}
