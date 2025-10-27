package eshop.com.eshopauthservice.web.dto;

public record UserRegisterRequest(String username,
                                  String email,
                                  String password,
                                  String confirmPassword ) {
}