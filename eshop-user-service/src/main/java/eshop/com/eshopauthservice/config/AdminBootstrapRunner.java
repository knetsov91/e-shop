package eshop.com.eshopauthservice.config;

import eshop.com.eshopauthservice.user.model.Role;
import eshop.com.eshopauthservice.user.model.User;
import eshop.com.eshopauthservice.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Creates the first admin account from ADMIN_USERNAME/ADMIN_EMAIL/ADMIN_PASSWORD on startup,
 * if one doesn't exist yet. There's no other way to obtain an admin account — the public
 * registration endpoint always assigns ROLE_USER by design — so this is the bootstrap path,
 * same pattern Keycloak/Grafana/Nextcloud use: whoever deploys the container controls the
 * first admin's credentials via the environment, not a hardcoded value baked into the image.
 */
@Component
public class AdminBootstrapRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrapRunner.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.username:}")
    private String adminUsername;

    @Value("${admin.email:}")
    private String adminEmail;

    @Value("${admin.password:}")
    private String adminPassword;

    public AdminBootstrapRunner(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.existsByRole(Role.ADMIN)) {
            return;
        }

        if (adminUsername.isBlank() || adminEmail.isBlank() || adminPassword.isBlank()) {
            log.warn("No admin account exists and ADMIN_USERNAME/ADMIN_EMAIL/ADMIN_PASSWORD " +
                    "are not set — skipping admin bootstrap. Set them to create the first admin account.");
            return;
        }

        User admin = new User();
        admin.setUsername(adminUsername);
        admin.setEmail(adminEmail);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);

        log.info("Bootstrapped initial admin account '{}'", adminUsername);
    }
}
