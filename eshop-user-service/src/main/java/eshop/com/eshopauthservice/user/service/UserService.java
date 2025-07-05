package eshop.com.eshopauthservice.user.service;

import eshop.com.eshopauthservice.user.model.User;
import eshop.com.eshopauthservice.user.repository.UserRepository;
import eshop.com.eshopauthservice.util.TimeProvider;
import eshop.com.eshopauthservice.web.dto.UserRegisterRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private TimeProvider timeProvider;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, TimeProvider timeProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.timeProvider = timeProvider;
    }

    public void register(UserRegisterRequest userRegisterRequest) {
        User  user = new User();
        user.setEmail(user.getEmail());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreatedAt(timeProvider.createdAt());
        user.setUpdatedAt(timeProvider.updatedAt());

        userRepository.save(user);

    }
}
