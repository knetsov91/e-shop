package eshop.com.eshopauthservice.config;

import eshop.com.eshopauthservice.user.model.Role;
import eshop.com.eshopauthservice.user.model.User;
import eshop.com.eshopauthservice.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminBootstrapRunnerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminBootstrapRunner adminBootstrapRunner;

    @Test
    void run_whenAdminAlreadyExists_thenDoesNothing() {
        when(userRepository.existsByRole(Role.ADMIN)).thenReturn(true);

        adminBootstrapRunner.run(null);

        verify(userRepository, never()).save(any());
    }

    @Test
    void run_whenNoAdminAndEnvVarsMissing_thenDoesNothing() {
        when(userRepository.existsByRole(Role.ADMIN)).thenReturn(false);
        ReflectionTestUtils.setField(adminBootstrapRunner, "adminUsername", "");
        ReflectionTestUtils.setField(adminBootstrapRunner, "adminEmail", "");
        ReflectionTestUtils.setField(adminBootstrapRunner, "adminPassword", "");

        adminBootstrapRunner.run(null);

        verify(userRepository, never()).save(any());
    }

    @Test
    void run_whenOnlyPasswordMissing_thenDoesNothing() {
        when(userRepository.existsByRole(Role.ADMIN)).thenReturn(false);
        ReflectionTestUtils.setField(adminBootstrapRunner, "adminUsername", "admin");
        ReflectionTestUtils.setField(adminBootstrapRunner, "adminEmail", "admin@example.com");
        ReflectionTestUtils.setField(adminBootstrapRunner, "adminPassword", "");

        adminBootstrapRunner.run(null);

        verify(userRepository, never()).save(any());
    }

    @Test
    void run_whenNoAdminAndEnvVarsSet_thenCreatesAdmin() {
        when(userRepository.existsByRole(Role.ADMIN)).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("encoded-secret");
        ReflectionTestUtils.setField(adminBootstrapRunner, "adminUsername", "admin");
        ReflectionTestUtils.setField(adminBootstrapRunner, "adminEmail", "admin@example.com");
        ReflectionTestUtils.setField(adminBootstrapRunner, "adminPassword", "secret123");

        adminBootstrapRunner.run(null);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getUsername()).isEqualTo("admin");
        assertThat(saved.getEmail()).isEqualTo("admin@example.com");
        assertThat(saved.getPassword()).isEqualTo("encoded-secret");
        assertThat(saved.getRole()).isEqualTo(Role.ADMIN);
    }
}
