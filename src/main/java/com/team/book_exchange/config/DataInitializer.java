package com.team.book_exchange.config;

import com.team.book_exchange.entity.Role;
import com.team.book_exchange.entity.User;
import com.team.book_exchange.enums.RoleName;
import com.team.book_exchange.repository.RoleRepository;
import com.team.book_exchange.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.EnumSet;
import java.util.HashSet;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.full-name}")
    private String adminFullName;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Bean
    public CommandLineRunner seedInitialData() {
        return args -> {
            seedRoles();
            seedAdminUser();
        };
    }

    private void seedRoles() {
        for (RoleName roleName : EnumSet.allOf(RoleName.class)) {
            roleRepository.findByName(roleName)
                .orElseGet(() -> roleRepository.save(Role.builder()
                    .name(roleName)
                    .build()));
        }
    }

    private void seedAdminUser() {
        if (userRepository.existsByEmail(adminEmail)) {
            return;
        }

        Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
            .orElseThrow(() -> new IllegalStateException("ROLE_ADMIN was not found during initialization"));

        User adminUser = User.builder()
            .fullName(adminFullName)
            .email(adminEmail)
            .password(passwordEncoder.encode(adminPassword))
            .enabled(true)
            .roles(new HashSet<>())
            .build();

        adminUser.getRoles().add(adminRole);

        userRepository.save(adminUser);
    }
}
