package eshop.com.eshoporderservice.config;

import eshop.com.eshoporderservice.order.model.OrderCommand;
import eshop.com.eshoporderservice.service.OrderCommandService;
import eshop.com.eshoporderservice.service.OrderQueryService;
import eshop.com.eshoporderservice.web.OrderCommandController;
import eshop.com.eshoporderservice.web.OrderQueryController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({OrderCommandController.class, OrderQueryController.class})
@Import(SecurityConfig.class)
@org.springframework.test.context.TestPropertySource(properties = {
        "spring.cloud.consul.enabled=false",
        "spring.cloud.consul.config.enabled=false",
        "spring.cloud.consul.discovery.enabled=false",
        "spring.config.import=optional:consul:"
})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderCommandService orderCommandService;

    @MockitoBean
    private OrderQueryService orderQueryService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void createOrder_whenUnauthenticated_thenReturns401() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .contentType("application/json")
                        .content("{\"product\":\"product-1\",\"quantity\":1}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createOrder_whenAuthenticated_thenReturns200() throws Exception {
        when(orderCommandService.createOrder(any())).thenReturn(new OrderCommand());

        mockMvc.perform(post("/api/v1/orders")
                        .with(jwt())
                        .contentType("application/json")
                        .content("{\"product\":\"product-1\",\"quantity\":1,\"amount\":19.99}"))
                .andExpect(status().isOk());
    }

    @Test
    void getAllOrders_whenAuthenticated_thenReturns200() throws Exception {
        when(orderQueryService.getAllOrders()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/orders").with(jwt()))
                .andExpect(status().isOk());
    }

    @Test
    void getAllOrders_whenUnauthenticated_thenReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isUnauthorized());
    }
}
