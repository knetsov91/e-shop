package eshop.com.eshoporderservice.web;

import eshop.com.eshoporderservice.config.SecurityConfig;
import eshop.com.eshoporderservice.order.model.OrderQuery;
import eshop.com.eshoporderservice.order.model.OrderStatus;
import eshop.com.eshoporderservice.service.OrderQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
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
        Page<OrderQuery> page = new PageImpl<>(List.of(
                new OrderQuery("order-1", "product-1", 2, "CONFIRMED"),
                new OrderQuery("order-2", "product-2", 1, "PENDING")
        ));
        when(orderQueryService.getAllOrders(any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/orders").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].orderId").value("order-1"))
                .andExpect(jsonPath("$.content[0].product").value("product-1"))
                .andExpect(jsonPath("$.content[0].quantity").value(2))
                .andExpect(jsonPath("$.content[0].status").value("CONFIRMED"))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void getAllOrders_whenNoOrdersExist_thenReturnsEmptyList() throws Exception {
        when(orderQueryService.getAllOrders(any())).thenReturn(Page.empty());

        mockMvc.perform(get("/api/v1/orders").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    void getOrderById_whenOrderExists_thenReturnsItAsJson() throws Exception {
        when(orderQueryService.getOrderById("order-1")).thenReturn(
                Optional.of(new OrderQuery("order-1", "product-1", 2, "CONFIRMED"))
        );

        mockMvc.perform(get("/api/v1/orders/order-1").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value("order-1"))
                .andExpect(jsonPath("$.product").value("product-1"))
                .andExpect(jsonPath("$.quantity").value(2))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void getOrderById_whenOrderDoesNotExist_thenReturns404() throws Exception {
        when(orderQueryService.getOrderById("missing-order")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/orders/missing-order").with(jwt()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllOrders_whenStatusFilterGiven_thenReturnsMatchingOrders() throws Exception {
        Page<OrderQuery> page = new PageImpl<>(List.of(
                new OrderQuery("order-1", "product-1", 2, "CONFIRMED")
        ));
        when(orderQueryService.getOrdersByStatus(any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/orders").param("status", "CONFIRMED").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].orderId").value("order-1"))
                .andExpect(jsonPath("$.content[0].status").value("CONFIRMED"));
    }

    @Test
    void getAllOrders_whenStatusFilterInvalid_thenReturns400() throws Exception {
        mockMvc.perform(get("/api/v1/orders").param("status", "BOGUS").with(jwt()))
                .andExpect(status().isBadRequest());
    }
}
