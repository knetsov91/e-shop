package eshop.com.eshoporderservice.web;

import eshop.com.eshoporderservice.config.SecurityConfig;
import eshop.com.eshoporderservice.order.model.OrderQuery;
import eshop.com.eshoporderservice.service.OrderQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderQueryController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
        "spring.cloud.consul.enabled=false",
        "spring.cloud.consul.config.enabled=false",
        "spring.cloud.consul.discovery.enabled=false",
        "spring.config.import=optional:consul:"
})
class OrderQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderQueryService orderQueryService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void getAllOrders_whenOrdersExist_thenReturnsThemAsJson() throws Exception {
        when(orderQueryService.getAllOrders()).thenReturn(List.of(
                new OrderQuery("order-1", "product-1", 2, "CONFIRMED"),
                new OrderQuery("order-2", "product-2", 1, "PENDING")
        ));

        mockMvc.perform(get("/api/v1/orders").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].orderId").value("order-1"))
                .andExpect(jsonPath("$[0].product").value("product-1"))
                .andExpect(jsonPath("$[0].quantity").value(2))
                .andExpect(jsonPath("$[0].status").value("CONFIRMED"));
    }
}
