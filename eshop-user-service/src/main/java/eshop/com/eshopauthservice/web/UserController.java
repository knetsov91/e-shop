package eshop.com.eshopauthservice.web;

import eshop.com.eshopauthservice.user.service.UserService;
import eshop.com.eshopauthservice.web.dto.UserRegisterRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRegisterRequest  userRegisterRequest) {
        userService.register(userRegisterRequest);

        return ResponseEntity.status(201).build();
    }
}
