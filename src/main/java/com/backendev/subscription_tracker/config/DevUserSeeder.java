package com.backendev.subscription_tracker.config;

import com.backendev.subscription_tracker.dto.RegistrationRequestDto;
import com.backendev.subscription_tracker.entity.Role;
import com.backendev.subscription_tracker.repository.UserRepository;
import com.backendev.subscription_tracker.service.AuthService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class DevUserSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AuthService authService;

    public DevUserSeeder(UserRepository userRepository, AuthService authService) {
        this.userRepository = userRepository;
        this.authService = authService;
    }

    @Override
    public void run(String... args) {
        if (!userRepository.existsByUsername("admin")) {
            authService.registerWithRole(
                    new RegistrationRequestDto("admin", "admin123"),
                    Role.ADMIN);
        }
        if (!userRepository.existsByUsername("user")) {
            authService.registerWithRole(
                    new RegistrationRequestDto("user", "user1234"),
                    Role.USER);
        }
    }
}