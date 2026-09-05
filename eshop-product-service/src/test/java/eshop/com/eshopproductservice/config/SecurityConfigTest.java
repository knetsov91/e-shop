package eshop.com.eshopproductservice.config;

import eshop.com.eshopproductservice.model.Product;
import eshop.com.eshopproductservice.service.ProductService;
import eshop.com.eshopproductservice.web.ProductController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
        "spring.cloud.consul.enabled=false",
        "spring.cloud.consul.config.enabled=false",
        "spring.cloud.consul.discovery.enabled=false",
        "spring.config.import=optional:consul:"
})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void getProducts_whenUnauthenticated_thenReturns200() throws Exception {
        when(productService.getProducts()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk());
    }

    @Test
    void createProduct_whenUnauthenticated_thenReturns401() throws Exception {
        mockMvc.perform(post("/api/v1/products")
                        .contentType("application/json")
                        .content("{\"name\":\"product-1\",\"description\":\"a product\",\"price\":9.99,\"quantity\":1}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createProduct_whenAuthenticated_thenReturns201() throws Exception {
        when(productService.createProduct(any())).thenReturn(new Product());

        mockMvc.perform(post("/api/v1/products")
                        .with(jwt())
                        .contentType("application/json")
                        .content("{\"name\":\"product-1\",\"description\":\"a product\",\"price\":9.99,\"quantity\":1}"))
                .andExpect(status().isCreated());
    }
}
