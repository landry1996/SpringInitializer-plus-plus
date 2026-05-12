package com.springforge.user.infrastructure;

import com.springforge.user.domain.Role;
import com.springforge.user.domain.User;
import com.springforge.user.domain.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminDataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminDataInitializer.class);

    private static final String DEFAULT_ADMIN_EMAIL = "admin@springforge.io";
    private static final String DEFAULT_ADMIN_PASSWORD = "Admin123!";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminDataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.findByEmail(DEFAULT_ADMIN_EMAIL).isEmpty()) {
            User admin = new User(
                    DEFAULT_ADMIN_EMAIL,
                    passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD),
                    "Admin",
                    "SpringForge",
                    Role.ADMIN
            );
            userRepository.save(admin);
            log.info("Default admin user created: {}", DEFAULT_ADMIN_EMAIL);
            log.warn("Please change the default admin password after first login!");
        }
    }
}
