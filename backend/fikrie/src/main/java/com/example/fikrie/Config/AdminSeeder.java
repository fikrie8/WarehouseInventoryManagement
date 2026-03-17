package com.example.fikrie.Config;

import com.example.fikrie.Model.Users;
import com.example.fikrie.Repository.UserRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Objects;
import java.util.Optional;

@Configuration
@Slf4j
public class AdminSeeder {

    @Bean
    CommandLineRunner createAdmin(UserRepo userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            String username = "admin";
            String adminEmail = System.getenv("ADMIN_EMAIL");
            String password = System.getenv("ADMIN_PASSWORD");
            if(password.isEmpty()) {
                throw new RuntimeException("ADMIN_PASSWORD environment variable not set");
            }
            Optional<Users> adminOptional = userRepository.findByUsername(username);
            if (adminOptional.isEmpty()) {
                log.debug("Creating ADMIN user");
                Users admin = new Users();
                admin.setUsername(username);
                admin.setPassword(passwordEncoder.encode(password));
                admin.setRole("ADMIN");
                admin.setEmail(adminEmail);
                userRepository.save(admin);
            }
        };
    }
}
