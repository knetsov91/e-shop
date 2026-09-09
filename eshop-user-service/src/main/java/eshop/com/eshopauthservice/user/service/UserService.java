package eshop.com.eshopauthservice.user.service;

import eshop.com.eshopauthservice.user.model.Role;
import eshop.com.eshopauthservice.user.model.User;
import eshop.com.eshopauthservice.user.repository.UserRepository;
import eshop.com.eshopauthservice.web.dto.RegisterRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(RegisterRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        // Public registration must never be able to grant admin — that's a privilege-escalation hole.
        user.setRole(Role.USER);
        return userRepository.save(user);
    }
}
