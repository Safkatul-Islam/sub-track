package com.backendev.subscription_tracker.service;

import com.backendev.subscription_tracker.dto.RegistrationRequestDto;
import com.backendev.subscription_tracker.dto.UserResponseDto;
import com.backendev.subscription_tracker.entity.Role;
import com.backendev.subscription_tracker.entity.User;
import com.backendev.subscription_tracker.exception.UserAlreadyExistsException;
import com.backendev.subscription_tracker.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponseDto register(RegistrationRequestDto request) {
        return registerWithRole(request, Role.USER);
    }

    public UserResponseDto registerWithRole(RegistrationRequestDto request, Role role) {
        if (userRepository.existsByUsername(request.username())) {
            throw new UserAlreadyExistsException(
                    "A user with the username '" + request.username() + "' already exists!");
        }

        User user = new User();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(role);

        User saved = userRepository.save(user);
        return new UserResponseDto(saved.getId(), saved.getUsername(), saved.getRole());
    }
}