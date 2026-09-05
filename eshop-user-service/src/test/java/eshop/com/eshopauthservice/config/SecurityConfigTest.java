package eshop.com.eshopauthservice.config;

import eshop.com.eshopauthservice.user.service.UserService;
import eshop.com.eshopauthservice.web.AuthController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(AuthorizationServerConfig.class)
@TestPropertySource(properties = {
        "spring.cloud.consul.enabled=false",
        "spring.cloud.consul.config.enabled=false",
        "spring.cloud.consul.discovery.enabled=false",
        "spring.config.import=optional:consul:",
        "eshop.service-client.secret=test-secret"
})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void register_whenUnauthenticated_thenReturns201() throws Exception {
        mockMvc.perform(post("/api/v1/users/register")
                        .contentType("application/json")
                        .content("{\"username\":\"jdoe\",\"password\":\"secret123\",\"email\":\"jdoe@example.com\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void protectedPath_whenUnauthenticated_thenRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/api/v1/users/profile"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }
}
