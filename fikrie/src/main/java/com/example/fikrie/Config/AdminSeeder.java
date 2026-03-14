package com.example.fikrie.Config;

import com.example.fikrie.Model.Users;
import com.example.fikrie.Repository.UserRepo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Objects;

@Configuration
public class AdminSeeder {

    @Bean
    CommandLineRunner createAdmin(UserRepo userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            String username = "admin";
            String password = System.getenv("ADMIN_PASSWORD");
            if(password.isEmpty()) {
                throw new RuntimeException("ADMIN_PASSWORD environment variable not set");
            }
            if (Objects.isNull(userRepository.findByUsername(username))) {
                Users admin = new Users();
                admin.setUsername(username);
                admin.setPassword(passwordEncoder.encode(password));
                admin.setRole("ADMIN");
                userRepository.save(admin);
            }
        };
    }
}
