package com.trader.journal_backend.config;

import com.trader.journal_backend.model.User;
import com.trader.journal_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
@RequiredArgsConstructor
@Slf4j
@Order(1)
public class UserInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            User admin = new User();
            admin.setEmail("admin@journal.com");
            admin.setPassword("123456"); 
            admin.setAdmin(true);
            userRepository.save(admin);
            log.info("USER_INIT | Created default admin user: admin@journal.com");
        } else {
            log.info("USER_INIT | Default user already exists. Skipping.");
        }
    }
}