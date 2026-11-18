package com.example.fikrie.Config;

import com.example.fikrie.Model.Users;
import com.example.fikrie.Repository.UserRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Value;

import java.util.Optional;

@Configuration
@Slf4j
public class AdminSeeder {

    @Value("${ADMIN_EMAIL}")
    private String adminEmail;

    @Value("${ADMIN_PASSWORD}")
    private String adminPassword;

    @Bean
    CommandLineRunner createAdmin(UserRepo userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            String username = "admin";

            if(adminPassword == null || adminPassword.isEmpty()) {
                throw new RuntimeException("ADMIN_PASSWORD environment variable is not set");
            }
            Optional<Users> adminOptional = userRepository.findByUsername(username);
            if (adminOptional.isEmpty()) {
                log.info("Creating ADMIN user with email: " + adminEmail);
                Users admin = new Users();
                admin.setUsername(username);
                admin.setPassword(passwordEncoder.encode(adminPassword));
                admin.setRole("ADMIN");
                admin.setEmail(adminEmail);
                userRepository.save(admin);
            }
        };
    }
}
