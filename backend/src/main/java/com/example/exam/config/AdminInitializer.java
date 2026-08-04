package com.example.exam.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.exam.model.User;
import com.example.exam.repository.UserRepository;

@Configuration
public class AdminInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminInitializer.class);

    @Bean
    CommandLineRunner createAdminUser(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            String adminUsername = "admin";
            if (userRepository.findByUsername(adminUsername).isEmpty()) {
                String envPassword = System.getenv("ADMIN_PASSWORD");
                if (envPassword == null || envPassword.isBlank()) {
                    envPassword = "adminpass";
                    LOGGER.warn("ADMIN_PASSWORD env var not set. Using default password: adminpass");
                }

                User adminUser = new User();
                adminUser.setUsername(adminUsername);
                adminUser.setPassword(passwordEncoder.encode(envPassword));
                adminUser.setRole("ROLE_ADMIN");
                adminUser.setFullName("Admin");
                adminUser.setEmail("admin@exam.com");

                userRepository.save(adminUser);
                LOGGER.info("Admin user 'admin' created with password: {}", envPassword);
            } else {
                LOGGER.info("Admin user 'admin' already exists.");
            }
        };
    }
}

